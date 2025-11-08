package com.imhungry.sillok.presentation.service

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.imhungry.sillok.data.model.realtime.LiveFeedbackDto
import com.imhungry.sillok.data.model.realtime.LiveSummaryDto
import com.imhungry.sillok.data.model.realtime.SseResponseType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.util.concurrent.TimeUnit
import kotlin.coroutines.cancellation.CancellationException

/**
 * SSE 연결 및 이벤트 처리를 담당하는 클래스
 */
@RequiresApi(Build.VERSION_CODES.O)
class SseManager(
    private val serviceScope: CoroutineScope,
    private val serviceEvents: MutableSharedFlow<ServiceEvent>
) {
    companion object {
        private const val TAG = "SseManager"
        private const val SSE_RECONNECT_INTERVAL = 8 * 60 * 1000L // 8분 (밀리초)
    }

    private val gson = Gson()
    private var eventSource: EventSource? = null
    private var sseReconnectJob: Job? = null
    private var sseServerUrl: String? = null
    private var sseMeetingId: Long? = null
    private var sseJwtToken: String? = null

    fun connect(serverUrl: String, meetingId: Long, jwtToken: String) {
        Log.d(TAG, "[SSE-1] SSE 연결 준비 시작")
        sseServerUrl = serverUrl
        sseMeetingId = meetingId
        sseJwtToken = jwtToken

        sseReconnectJob?.cancel()

        serviceScope.launch {
            try {
                Log.d(TAG, "[SSE-1-1] SSE 연결 시작")
                val sseUrl = "${serverUrl}api/v1/meetings/$meetingId/events/subscribe"
                Log.d(TAG, "[SSE-1-2] SSE URL: $sseUrl")

                val request = Request.Builder()
                    .url(sseUrl)
                    .addHeader("Authorization", "Bearer $jwtToken")
                    .addHeader("Accept", "text/event-stream")
                    .build()

                val sseClient = OkHttpClient.Builder()
                    .readTimeout(0, TimeUnit.SECONDS)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .build()

                Log.d(TAG, "[SSE-1-3] SSE EventSource 생성 시작")
                eventSource = EventSources.createFactory(sseClient)
                    .newEventSource(request, object : EventSourceListener() {
                        override fun onOpen(eventSource: EventSource, response: Response) {
                            Log.d(TAG, "========================================")
                            Log.d(TAG, "[SSE-2] SSE 연결 성공!")
                            Log.d(TAG, "  - Response Code: ${response.code}")
                            Log.d(TAG, "========================================")
                        }

                        @RequiresApi(Build.VERSION_CODES.O)
                        override fun onEvent(
                            eventSource: EventSource,
                            id: String?,
                            type: String?,
                            data: String
                        ) {
                            Log.d(
                                TAG,
                                "[SSE-3] SSE 이벤트 수신: type=$type, id=$id, data=${data.take(100)}..."
                            )
                            parseSseEvent(type, data)
                        }

                        override fun onFailure(
                            eventSource: EventSource,
                            t: Throwable?,
                            response: Response?
                        ) {
                            Log.e(TAG, "[SSE-실패] SSE 실패: ${t?.message}", t)
                            Log.e(TAG, "  - Response: ${response?.code}")
                        }
                    })

                Log.d(TAG, "[SSE-1-4] SSE EventSource 생성 완료")
                Log.d(TAG, "[SSE-1-5] SSE 재연결 Job 시작")
                startSseReconnectJob()
                Log.d(TAG, "[SSE-1 완료] SSE 연결 설정 완료")
            } catch (e: Exception) {
                Log.e(TAG, "[SSE-예외] SSE 예외: ${e.message}", e)
            }
        }
    }

    private fun startSseReconnectJob() {
        sseReconnectJob?.cancel()
        sseReconnectJob = serviceScope.launch(Dispatchers.IO) {
            try {
                while (coroutineContext.isActive) {
                    delay(SSE_RECONNECT_INTERVAL)

                    val serverUrl = sseServerUrl
                    val meetingId = sseMeetingId
                    val jwtToken = sseJwtToken

                    if (serverUrl != null && meetingId != null && jwtToken != null) {
                        Log.d(TAG, "SSE 재연결 시작 (8분 주기)")
                        eventSource?.cancel()
                        eventSource = null
                        connect(serverUrl, meetingId, jwtToken)
                    } else {
                        break
                    }
                }
            } catch (e: CancellationException) {
                Log.d(TAG, "SSE 재연결 Job 취소됨")
            } catch (e: Exception) {
                Log.e(TAG, "SSE 재연결 Job 에러: ${e.message}", e)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun parseSseEvent(type: String?, data: String) {
        val sseType = type.toSseResponseType()
        when (sseType) {
            SseResponseType.CONNECTED -> {
                Log.d(TAG, "SSE 연결 확인: $data")
            }

            SseResponseType.PARTICIPATION_RATE -> {
                try {
                    val participationRates = gson.fromJson<Map<Long, Double>>(
                        data,
                        object : TypeToken<Map<Long, Double>>() {}.type
                    )
                    serviceScope.launch {
                        serviceEvents.emit(ServiceEvent.ParticipationRate(participationRates))
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "참여율 파싱 실패", e)
                }
            }

            SseResponseType.FEEDBACK -> {
                try {
                    val feedback = gson.fromJson(data, LiveFeedbackDto::class.java)
                    serviceScope.launch {
                        serviceEvents.emit(ServiceEvent.Feedback(feedback))
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "피드백 파싱 실패", e)
                }
            }

            SseResponseType.SUMMARY -> {
                try {
                    val summary = gson.fromJson(data, LiveSummaryDto::class.java)
                    serviceScope.launch {
                        serviceEvents.emit(ServiceEvent.Summary(summary))
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "요약 파싱 실패", e)
                }
            }

            else -> {
                Log.w(TAG, "알 수 없는 SSE 타입: $type")
            }
        }
    }

    fun disconnect() {
        // SSE 재연결 Job 취소
        sseReconnectJob?.cancel()
        sseReconnectJob = null

        // SSE 연결 해제
        eventSource?.cancel()
        eventSource = null

        sseServerUrl = null
        sseMeetingId = null
        sseJwtToken = null

        Log.d(TAG, "SSE 연결 해제 완료")
    }

    private fun String?.toSseResponseType(): SseResponseType? {
        return try {
            SseResponseType.valueOf(this ?: "")
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}

