package com.imhungry.jjongseol.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.imhungry.jjongseol.BuildConfig
import com.imhungry.jjongseol.R
import com.imhungry.jjongseol.data.model.feedback.dto.FeedbackDto
import com.imhungry.jjongseol.data.model.participationrate.dto.ParticipationRateDto
import com.imhungry.jjongseol.data.model.segment.DiarizedSegment
import com.imhungry.jjongseol.data.model.summary.dto.SummaryDto
import com.imhungry.jjongseol.data.network.client.AudioWebSocketClient
import com.imhungry.jjongseol.data.network.config.AppPrefs
import com.imhungry.jjongseol.data.repository.AgendaSocketEventRepository
import com.imhungry.jjongseol.data.repository.DiarizedSegmentRepository
import com.imhungry.jjongseol.data.repository.event.ErrorEventRepository
import com.imhungry.jjongseol.data.repository.event.FeedbackEventRepository
import com.imhungry.jjongseol.data.repository.LoginRepository
import com.imhungry.jjongseol.data.repository.event.MeetingNoteCreatedEventBus
import com.imhungry.jjongseol.data.repository.event.MeetingStartTimeEventBus
import com.imhungry.jjongseol.data.repository.event.ParticipationRateEventRepository
import com.imhungry.jjongseol.data.repository.event.SummaryEventRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class MeetingSseService : Service() {
    @Inject
    lateinit var loginRepository: LoginRepository
    @Inject
    lateinit var feedbackEventRepository: FeedbackEventRepository
    @Inject
    lateinit var summaryEventRepository: SummaryEventRepository

    private var summaryEventSource: EventSource? = null
    private var feedbackEventSource: EventSource? = null
    private var participationRateEventSource: EventSource? = null

    private var reconnectHandler: android.os.Handler? = null
    private var reconnectRunnable: Runnable? = null

    private var currentMeetingId: Long = -1L
    private var isServiceStopped: Boolean = false

    private var isConnecting = false

    companion object {
        const val CHANNEL_ID = "meeting_sse_channel"
        const val CHANNEL_NAME = "회의 SSE 알림"
        const val ACTION_SET_MIC = "ACTION_SET_MIC"
        const val EXTRA_MIC_ENABLED = "EXTRA_MIC_ENABLED"
    }

    private var audioWsClient: AudioWebSocketClient? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private var currentMicEnabled: Boolean = true
    private lateinit var appPrefs: AppPrefs
    private var reconnectAttempts = 0
    private val maxReconnectAttempts = 3

    private fun createNotification(content: String): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = "회의 중 포그라운드 서비스"
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(content)
            .setSmallIcon(R.drawable.notification_logo)
            .setOngoing(true)
            .build()
    }

    private fun startReconnectTimer(meetingId: Long) {
        stopReconnectTimer()
        reconnectHandler = android.os.Handler(mainLooper)
        reconnectRunnable = Runnable {
            reconnectSse(meetingId)
            startReconnectTimer(meetingId)
        }
        reconnectHandler?.postDelayed(reconnectRunnable!!, 8 * 60 * 1000L)
    }

    private fun stopReconnectTimer() {
        reconnectHandler?.removeCallbacks(reconnectRunnable ?: return)
        reconnectHandler = null
        reconnectRunnable = null
    }

    private fun connectSse(meetingId: Long) {
        if (isConnecting) {
            Log.d("Audio", "Already connecting, skip!")
            return
        }
        Log.d("Audio", "SSE 연결 시도")
        isConnecting = true

        summaryEventSource?.cancel()
        feedbackEventSource?.cancel()
        participationRateEventSource?.cancel()
        summaryEventSource = null
        feedbackEventSource = null
        participationRateEventSource = null
        if (isServiceStopped || meetingId == -1L) return

        val client = OkHttpClient.Builder()
            .readTimeout(15, TimeUnit.MINUTES)
            .addInterceptor { chain ->
                val token = loginRepository.getToken() ?: ""
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                chain.proceed(request)
            }
            .build()

        val summaryRequest = Request.Builder()
            .url(BuildConfig.BASE_URL + "api/sse/subscribe/summary/$meetingId")
            .build()
        val feedbackRequest = Request.Builder()
            .url(BuildConfig.BASE_URL + "api/sse/subscribe/feedback/$meetingId")
            .build()
        val participationRateRequest = Request.Builder()
            .url(BuildConfig.BASE_URL + "api/sse/subscribe/participation-rate/$meetingId")
            .build()

        val listener = object : EventSourceListener() {
            override fun onEvent(source: EventSource, id: String?, type: String?, data: String) {
                try {
                    when (type) {
                        "SUMMARY" -> {
                            val json = JSONObject(data)
                            val summary = SummaryDto(
                                timestamp = json.optString("timestamp"),
                                summary = json.optString("summary")
                            )
                            CoroutineScope(Dispatchers.IO).launch {
                                summaryEventRepository.emitSummary(summary)
                            }
                        }
                        "FEEDBACK" -> {
                            val json = JSONObject(data)
                            val feedback = FeedbackDto(
                                timestamp = json.optString("timestamp"),
                                comment = json.optString("comment")
                            )
                            CoroutineScope(Dispatchers.IO).launch {
                                feedbackEventRepository.emitFeedback(feedback)
                            }
                        }
                        "PARTICIPATION_RATE" -> {
                            val json = JSONObject(data)
                            val timestamp = json.optString("timestamp")
                            val ratesObj = json.optJSONObject("participationRates") ?: JSONObject()
                            val list = mutableListOf<ParticipationRateDto>()
                            val keys = ratesObj.keys()
                            while (keys.hasNext()) {
                                val key = keys.next()
                                val userId = key.toLongOrNull() ?: continue
                                val rate = ratesObj.optDouble(key, 0.0)
                                list.add(ParticipationRateDto(userId, rate))
                            }
                            CoroutineScope(Dispatchers.IO).launch {
                                list.forEach {
                                    ParticipationRateEventRepository.emitParticipationRate(it)
                                }
                            }
                        }
                        "CONNECT" -> {
                            reconnectAttempts = 0
                            Log.d("Audio", "CONNECT: $data")
                        }
                        else -> {
                            Log.w("Audio", "SSE 응답 : $data")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("Audio", "Exception: ${e.message}", e)
                    ErrorEventRepository.emitError("Exception: ${e.message}")
                }
            }

            override fun onClosed(source: EventSource) {
                Log.d("Audio", "SSE 연결 종료, 재연결 시도")
                isConnecting = false
                //reconnectSse(meetingId)
            }
            override fun onFailure(source: EventSource, t: Throwable?, response: Response?) {
                Log.e(
                    "Audio",
                    "SSE 연결 실패: ${t?.message}, response=${response?.code} / ${response?.message}", t
                )
                isConnecting = false
                ErrorEventRepository.emitError("response=${response?.code} / ${response?.message}")

                //reconnectSse(meetingId)
            }
        }

        summaryEventSource = EventSources.createFactory(client)
            .newEventSource(summaryRequest, listener)
        feedbackEventSource = EventSources.createFactory(client)
            .newEventSource(feedbackRequest, listener)
        participationRateEventSource = EventSources.createFactory(client)
            .newEventSource(participationRateRequest, listener)
        startReconnectTimer(meetingId)
    }

    private fun reconnectSse(meetingId: Long) {
        if (isConnecting) {
            Log.d("MeetingSseService", "Reconnect requested while already connecting")
            return
        }
        reconnectAttempts++
        if (reconnectAttempts > maxReconnectAttempts) {
            ErrorEventRepository.emitError("서버 내부 오류입니다. 관리자에게 문의해주세요.")
            return
        }
        summaryEventSource?.cancel()
        feedbackEventSource?.cancel()
        participationRateEventSource?.cancel()
        if (isServiceStopped || meetingId == -1L) return

        android.os.Handler(mainLooper).postDelayed({
            connectSse(meetingId)
        }, 1000)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("Audio", "포그라운드 서비스 새로 생성")
        appPrefs = AppPrefs(applicationContext)
        currentMeetingId = intent?.getLongExtra("meetingId", -1L) ?: -1L
        isServiceStopped = false
        startForeground(1, createNotification("회의 진행 중.."))

        if (intent?.action == ACTION_SET_MIC) {
            val micEnabled = intent.getBooleanExtra(EXTRA_MIC_ENABLED, true)
            val id = intent.getLongExtra("meetingId", currentMeetingId)
            currentMeetingId = id
            setMicEnabled(micEnabled)
            return START_STICKY
        }
        if (currentMeetingId == -1L) {
            appPrefs.setMeetingForegroundServiceRunning(false)
            stopSelf()
            return START_NOT_STICKY
        }
        appPrefs.setMeetingForegroundServiceRunning(true)
        appPrefs.setRunningMeetingId(currentMeetingId)
        connectSse(currentMeetingId)
        val token = loginRepository.getToken() ?: ""
        connectAudioWebSocket(currentMeetingId, token)
        currentMicEnabled = appPrefs.getMicEnabled(currentMeetingId)
        setMicEnabled(currentMicEnabled)
        return START_STICKY
    }

    fun setMicEnabled(enabled: Boolean) {
        appPrefs.setMicEnabled(currentMeetingId, enabled)
        currentMicEnabled = enabled
        audioWsClient?.micEnabled = enabled
        if (enabled) {
            audioWsClient?.resumeEncoding()
        } else {
            audioWsClient?.pauseEncoding()
        }
    }

    private fun connectAudioWebSocket(meetingId: Long, token: String) {
        val url = "ws://${BuildConfig.WS_HOST}/ws/meeting/audio/$meetingId?token=$token"
        val micEnabled = appPrefs.getMicEnabled(meetingId)
        audioWsClient?.disconnect()
        audioWsClient = AudioWebSocketClient(
            context = this,
            url = url,
            meetingId = meetingId,
            scope = serviceScope,
            isServiceStopped = { isServiceStopped },
            onError = { errMsg ->
                ErrorEventRepository.emitError(errMsg)
            },
            onMessage = { msg ->
                when (msg) {
                    "MEETING_RECORD_MADED" -> {
                        stopAllConnections()
                    }
                    "MEETING_COMPLETED" -> {
                        notifyMeetingNoteCreated()
                    }
                }
            },
            onNewDiarizedSegment = { chatMessage ->
                serviceScope.launch { DiarizedSegmentRepository.emit(chatMessage) }
            },
            onMeetingStartTime = { startTimeMillis ->
                MeetingStartTimeEventBus.send(meetingId, startTimeMillis)
            },
            onAgendaUpdated = { updateDto ->
                serviceScope.launch {
                    AgendaSocketEventRepository.emitAgendaUpdate(updateDto)
                }
            },
            micEnabled = micEnabled
        )
        audioWsClient?.connect()
    }

    private fun clearMeetingServiceState() {
        appPrefs.setMeetingForegroundServiceRunning(false)
    }

    override fun onDestroy() {
        Log.d("Audio", "SSE 종료")
        clearMeetingServiceState()
        isServiceStopped = true
        currentMeetingId = -1L
        stopReconnectTimer()
        summaryEventSource?.cancel()
        feedbackEventSource?.cancel()
        participationRateEventSource?.cancel()
        summaryEventSource = null
        feedbackEventSource = null
        participationRateEventSource = null
        audioWsClient?.isClosedByUser = true
        audioWsClient?.stop()
        audioWsClient = null
        stopForeground(true)
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.d("Audio", "SSE 종료")
        clearMeetingServiceState()
        isServiceStopped = true
        currentMeetingId = -1L
        stopReconnectTimer()
        summaryEventSource?.cancel()
        feedbackEventSource?.cancel()
        participationRateEventSource?.cancel()
        summaryEventSource = null
        feedbackEventSource = null
        participationRateEventSource = null
        audioWsClient?.isClosedByUser = true
        audioWsClient?.stop()
        audioWsClient = null
        stopForeground(true)
        super.onTaskRemoved(rootIntent)
    }

    private fun stopAllConnections() {
        Log.i("Audio", "회의 종료됨: SSE/AudioWebSocket 모두 종료")
        clearMeetingServiceState()
        isServiceStopped = true
        currentMeetingId = -1L
        stopReconnectTimer()
        summaryEventSource?.cancel()
        feedbackEventSource?.cancel()
        participationRateEventSource?.cancel()
        summaryEventSource = null
        feedbackEventSource = null
        participationRateEventSource = null
        audioWsClient?.isClosedByUser = true
        audioWsClient?.stop()
        audioWsClient = null
        stopForeground(true)
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun notifyMeetingNoteCreated() {
        MeetingNoteCreatedEventBus.send(currentMeetingId)
    }
}
