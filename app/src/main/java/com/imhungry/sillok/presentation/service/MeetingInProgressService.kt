package com.imhungry.sillok.presentation.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.imhungry.sillok.R
import com.imhungry.sillok.data.model.realtime.ErrorResponse
import com.imhungry.sillok.data.model.realtime.LiveFeedbackDto
import com.imhungry.sillok.data.model.realtime.LiveSummaryDto
import com.imhungry.sillok.data.model.realtime.RealtimeSegmentDto
import com.imhungry.sillok.data.model.realtime.SocketResponse
import com.imhungry.sillok.data.model.realtime.SocketResponseType
import com.imhungry.sillok.data.model.realtime.SseResponseType
import com.imhungry.sillok.presentation.util.DateTimeUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import okio.ByteString.Companion.toByteString
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.coroutineContext

@AndroidEntryPoint
@RequiresApi(Build.VERSION_CODES.O)
class MeetingInProgressService : Service() {
    
    companion object {
        private const val TAG = "MeetingInProgressService"
        private const val CHANNEL_ID = "meeting_in_progress_channel"
        private const val NOTIFICATION_ID = 1001
        
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_TOGGLE_MIC = "ACTION_TOGGLE_MIC"
        
        const val EXTRA_SERVER_URL = "EXTRA_SERVER_URL"
        const val EXTRA_MEETING_ID = "EXTRA_MEETING_ID"
        const val EXTRA_JWT_TOKEN = "EXTRA_JWT_TOKEN"
        
        // Service 이벤트를 ViewModel에 전달하기 위한 Flow
        private val _serviceEvents = MutableSharedFlow<ServiceEvent>()
        val serviceEvents: SharedFlow<ServiceEvent> = _serviceEvents.asSharedFlow()
        
        // Service 인스턴스 접근용
        @Volatile
        private var instance: MeetingInProgressService? = null
        
        fun getInstance(): MeetingInProgressService? = instance
    }
    
    // Service 이벤트 타입 정의
    sealed class ServiceEvent {
        data class ConnectionEstablished(val lastProcessedChunkId: Long?) : ServiceEvent()
        data class DiarizedSegment(val segment: RealtimeSegmentDto) : ServiceEvent()
        data object MeetingCompleted : ServiceEvent()
        data class MeetingNoteCreated(val message: String?) : ServiceEvent()
        data class ParticipationRate(val rates: Map<Long, Double>) : ServiceEvent()
        data class Feedback(val feedback: LiveFeedbackDto) : ServiceEvent()
        data class Summary(val summary: LiveSummaryDto) : ServiceEvent()
        data class Error(val error: ErrorResponse?) : ServiceEvent()
    }
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    private val gson = Gson()
    private var webSocket: WebSocket? = null
    private var eventSource: EventSource? = null
    
    // SSE 재연결 관련
    private var sseReconnectJob: Job? = null
    private var sseServerUrl: String? = null
    private var sseMeetingId: Long? = null
    private var sseJwtToken: String? = null
    private val sseReconnectInterval = 8 * 60 * 1000L // 8분 (밀리초)
    
    // AudioRecord 관련
    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private var isRecording = false
    private var micEnabled = true
    
    private val sampleRate = 16000
    private val frameSize = 16000
    private val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    ).coerceAtLeast(frameSize)
    
    // 청크 ID 카운터
    private val chunkIdCounter = AtomicLong(0)
    
    private var currentMeetingId: Long = 1L
    private var packetDir: File? = null
    
    private val okHttpClient = OkHttpClient.Builder()
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannel()
        Log.d(TAG, "Service 생성됨")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "========================================")
        Log.d(TAG, "[Service] onStartCommand 호출: action=${intent?.action}")
        Log.d(TAG, "========================================")
        when (intent?.action) {
            ACTION_START -> {
                Log.d(TAG, "[Service-1] ACTION_START 처리 시작")
                val serverUrl = intent.getStringExtra(EXTRA_SERVER_URL)
                val meetingId = intent.getLongExtra(EXTRA_MEETING_ID, -1L)
                val jwtToken = intent.getStringExtra(EXTRA_JWT_TOKEN)
                
                Log.d(TAG, "[Service-1-1] Intent 파라미터 확인: serverUrl=${serverUrl?.take(30)}..., meetingId=$meetingId, jwtToken=${if (jwtToken != null) "있음" else "null"}")
                
                if (serverUrl != null && meetingId != -1L && jwtToken != null) {
                    Log.d(TAG, "[Service-1-2] Foreground Service 시작")
                    startForeground(NOTIFICATION_ID, createNotification())
                    Log.d(TAG, "[Service-1-3] 연결 시작 (WebSocket + SSE)")
                    startConnections(serverUrl, meetingId, jwtToken)
                    Log.d(TAG, "[Service-1 완료] ACTION_START 처리 완료")
                } else {
                    Log.e(TAG, "[Service-1 실패] 필수 파라미터 누락: serverUrl=$serverUrl, meetingId=$meetingId, jwtToken=${if (jwtToken != null) "있음" else "null"}")
                }
            }
            ACTION_STOP -> {
                stopConnections()
                stopForeground(true)
                stopSelf()
            }
            ACTION_TOGGLE_MIC -> {
                micEnabled = !micEnabled
                Log.d(TAG, "마이크 토글: ${if (micEnabled) "켜짐" else "꺼짐"}")
            }
        }
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        instance = null
        stopConnections()
        serviceScope.cancel()
        Log.d(TAG, "Service 종료됨")
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "회의 진행 중",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "회의 진행 중 알림"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("회의 진행 중")
            .setContentText("회의가 진행 중입니다")
            .setSmallIcon(R.drawable.icon)
            .setOngoing(true)
            .build()
    }
    
    private fun startConnections(serverUrl: String, meetingId: Long, jwtToken: String) {
        Log.d(TAG, "[Service-1-3-1] startConnections 시작: meetingId=$meetingId")
        currentMeetingId = meetingId
        packetDir = File(cacheDir, "audio_packets/$meetingId").apply {
            if (!exists()) mkdirs()
            Log.d(TAG, "[Service-1-3-2] 오디오 패킷 디렉토리 준비: ${this.absolutePath}")
        }
        
        serviceScope.launch {
            // WebSocket 연결
            Log.d(TAG, "[Service-1-3-3] WebSocket 연결 시작")
            connectWebSocket(serverUrl, meetingId, jwtToken)
            
            // SSE 연결
            Log.d(TAG, "[Service-1-3-4] SSE 연결 시작")
            connectSse(serverUrl, meetingId, jwtToken)
            Log.d(TAG, "[Service-1-3 완료] startConnections 완료")
        }
    }
    
    private fun connectWebSocket(serverUrl: String, meetingId: Long, jwtToken: String) {
        serviceScope.launch {
            try {
                Log.d(TAG, "[Service-WebSocket-1] WebSocket 연결 시작")
                val wsUrl = "${serverUrl}ws/v1/meeting/$meetingId/audio"
                Log.d(TAG, "[Service-WebSocket-1-1] WebSocket URL: $wsUrl")
                
                val request = Request.Builder()
                    .url(wsUrl)
                    .addHeader("Authorization", "Bearer $jwtToken")
                    .build()
                
                Log.d(TAG, "[Service-WebSocket-1-2] WebSocket 요청 생성 완료")
                webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        Log.d(TAG, "========================================")
                        Log.d(TAG, "[Service-WebSocket-2] WebSocket 연결 성공!")
                        Log.d(TAG, "  - Response Code: ${response.code}")
                        Log.d(TAG, "========================================")
                    }
                    
                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onMessage(webSocket: WebSocket, text: String) {
                        Log.d(TAG, "[Service-WebSocket-3] WebSocket 메시지 수신: ${text.take(100)}...")
                        parseWebSocketMessage(text, webSocket)
                    }
                    
                    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                        Log.e(TAG, "[Service-WebSocket-실패] WebSocket 연결 실패: ${t.message}", t)
                        Log.e(TAG, "  - Response: ${response?.code}")
                    }
                })
                Log.d(TAG, "[Service-WebSocket-1-3] WebSocket 리스너 등록 완료")
            } catch (e: Exception) {
                Log.e(TAG, "[Service-WebSocket-예외] WebSocket 연결 예외: ${e.message}", e)
            }
        }
    }
    
    private fun connectSse(serverUrl: String, meetingId: Long, jwtToken: String) {
        Log.d(TAG, "[Service-SSE-1] SSE 연결 준비 시작")
        sseServerUrl = serverUrl
        sseMeetingId = meetingId
        sseJwtToken = jwtToken
        
        sseReconnectJob?.cancel()
        
        serviceScope.launch {
            try {
                Log.d(TAG, "[Service-SSE-1-1] SSE 연결 시작")
                val sseUrl = "${serverUrl}api/v1/meetings/$meetingId/events/subscribe"
                Log.d(TAG, "[Service-SSE-1-2] SSE URL: $sseUrl")
                
                val request = Request.Builder()
                    .url(sseUrl)
                    .addHeader("Authorization", "Bearer $jwtToken")
                    .addHeader("Accept", "text/event-stream")
                    .build()
                
                val sseClient = OkHttpClient.Builder()
                    .readTimeout(0, TimeUnit.SECONDS)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .build()
                
                Log.d(TAG, "[Service-SSE-1-3] SSE EventSource 생성 시작")
                eventSource = EventSources.createFactory(sseClient)
                    .newEventSource(request, object : EventSourceListener() {
                        override fun onOpen(eventSource: EventSource, response: Response) {
                            Log.d(TAG, "========================================")
                            Log.d(TAG, "[Service-SSE-2] SSE 연결 성공!")
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
                            Log.d(TAG, "[Service-SSE-3] SSE 이벤트 수신: type=$type, id=$id, data=${data.take(100)}...")
                            parseSseEvent(type, data)
                        }
                        
                        override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                            Log.e(TAG, "[Service-SSE-실패] SSE 실패: ${t?.message}", t)
                            Log.e(TAG, "  - Response: ${response?.code}")
                        }
                    })
                
                Log.d(TAG, "[Service-SSE-1-4] SSE EventSource 생성 완료")
                Log.d(TAG, "[Service-SSE-1-5] SSE 재연결 Job 시작")
                startSseReconnectJob()
                Log.d(TAG, "[Service-SSE-1 완료] SSE 연결 설정 완료")
            } catch (e: Exception) {
                Log.e(TAG, "[Service-SSE-예외] SSE 예외: ${e.message}", e)
            }
        }
    }
    
    private fun startSseReconnectJob() {
        sseReconnectJob?.cancel()
        sseReconnectJob = serviceScope.launch(Dispatchers.IO) {
            try {
                while (coroutineContext.isActive) {
                    delay(sseReconnectInterval)
                    
                    val serverUrl = sseServerUrl
                    val meetingId = sseMeetingId
                    val jwtToken = sseJwtToken
                    
                    if (serverUrl != null && meetingId != null && jwtToken != null) {
                        Log.d(TAG, "SSE 재연결 시작 (8분 주기)")
                        eventSource?.cancel()
                        eventSource = null
                        connectSse(serverUrl, meetingId, jwtToken)
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
    private fun parseWebSocketMessage(jsonString: String, webSocket: WebSocket) {
        try {
            val jsonObject = JsonParser.parseString(jsonString).asJsonObject
            val typeString = jsonObject.get("type")?.asString
            val socketType = typeString.toSocketResponseType()
            
            when (socketType) {
                SocketResponseType.CONNECTION_ESTABLISHED -> {
                    val response = gson.fromJson<SocketResponse<Long>>(
                        jsonString,
                        object : TypeToken<SocketResponse<Long>>() {}.type
                    )
                    handleConnectionEstablished(response.data, webSocket)
                }
                SocketResponseType.DIARIZED_SEGMENT -> {
                    val response = gson.fromJson<SocketResponse<RealtimeSegmentDto>>(
                        jsonString,
                        object : TypeToken<SocketResponse<RealtimeSegmentDto>>() {}.type
                    )
                    response.data?.let { segment ->
                        serviceScope.launch {
                            _serviceEvents.emit(ServiceEvent.DiarizedSegment(segment))
                        }
                    }
                }
                SocketResponseType.MEETING_COMPLETED -> {
                    // 회의 완료 시 녹음과 SSE 연결 해제 (Service는 WebSocket 연결 유지하여 회의록 생성 완료 기다림)
                    stopRecording()
                    disconnectSseConnection()
                    
                    // 청크 파일 디렉토리 삭제
                    deleteChunkFiles()
                    
                    serviceScope.launch {
                        _serviceEvents.emit(ServiceEvent.MeetingCompleted)
                    }
                }
                SocketResponseType.MEETING_NOTE_CREATED -> {
                    val response = gson.fromJson<SocketResponse<String>>(
                        jsonString,
                        object : TypeToken<SocketResponse<String>>() {}.type
                    )
                    serviceScope.launch {
                        _serviceEvents.emit(ServiceEvent.MeetingNoteCreated(response.data))
                    }
                }
                SocketResponseType.ERROR -> {
                    val response = gson.fromJson<SocketResponse<ErrorResponse>>(
                        jsonString,
                        object : TypeToken<SocketResponse<ErrorResponse>>() {}.type
                    )
                    serviceScope.launch {
                        _serviceEvents.emit(ServiceEvent.Error(response.data))
                    }
                }
                else -> {
                    Log.w(TAG, "알 수 없는 메시지 타입: $typeString")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "메시지 파싱 실패: ${e.message}", e)
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
                        _serviceEvents.emit(ServiceEvent.ParticipationRate(participationRates))
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "참여율 파싱 실패", e)
                }
            }
            SseResponseType.FEEDBACK -> {
                try {
                    val feedback = gson.fromJson(data, LiveFeedbackDto::class.java)
                    serviceScope.launch {
                        _serviceEvents.emit(ServiceEvent.Feedback(feedback))
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "피드백 파싱 실패", e)
                }
            }
            SseResponseType.SUMMARY -> {
                try {
                    val summary = gson.fromJson(data, LiveSummaryDto::class.java)
                    serviceScope.launch {
                        _serviceEvents.emit(ServiceEvent.Summary(summary))
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
    
    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleConnectionEstablished(lastProcessedChunkId: Long?, webSocket: WebSocket) {
        Log.d(TAG, "========================================")
        Log.d(TAG, "[Service-WebSocket-4] 연결 확립 처리 시작")
        Log.d(TAG, "  - 서버 마지막 처리 청크 ID: ${lastProcessedChunkId ?: "없음 (첫 연결)"}")
        Log.d(TAG, "========================================")
        
        serviceScope.launch(Dispatchers.IO) {
            try {
                // 재전송이 필요한 청크 확인 및 재전송
                Log.d(TAG, "[Service-WebSocket-4-1] 재전송 필요한 청크 확인 시작")
                val savedChunks = getSavedChunksForRetransmission(lastProcessedChunkId)
                Log.d(TAG, "[Service-WebSocket-4-1 완료] 재전송 필요한 청크: ${savedChunks.size}개")
                
                // 재전송이 필요한 경우 먼저 재전송 완료 후 녹음 시작
                if (savedChunks.isNotEmpty()) {
                    Log.d(TAG, "[Service-WebSocket-4-2] 청크 재전송 시작 (녹음 시작 전)")
                    retransmitMissingChunks(webSocket, savedChunks)
                    Log.d(TAG, "[Service-WebSocket-4-2 완료] 청크 재전송 완료 - 이제 녹음 시작 가능")
                    
                    // 청크 ID 카운터를 재전송한 마지막 청크 다음으로 설정
                    val lastRetransmittedId = savedChunks.maxOfOrNull { it.chunkId } ?: -1
                    chunkIdCounter.set(lastRetransmittedId + 1)
                    Log.d(TAG, "[Service-WebSocket-4-3] 청크 ID 카운터 설정: ${lastRetransmittedId + 1}")
                } else {
                    Log.d(TAG, "[Service-WebSocket-4-2 스킵] 재전송 필요한 청크 없음 - 바로 녹음 시작 가능")
                    
                    // 청크 ID 카운터 초기화
                    if (lastProcessedChunkId != null) {
                        chunkIdCounter.set(lastProcessedChunkId + 1)
                        Log.d(TAG, "[Service-WebSocket-4-3] 청크 ID 카운터 설정: ${lastProcessedChunkId + 1} (서버 기준)")
                    } else {
                        chunkIdCounter.set(0)
                        Log.d(TAG, "[Service-WebSocket-4-3] 청크 ID 카운터 설정: 0 (첫 연결)")
                    }
                }
                
                // 재전송 완료 후 실시간 녹음 시작
                Log.d(TAG, "[Service-WebSocket-4-4] 재전송 완료 후 실시간 녹음 시작")
                withContext(Dispatchers.Main) {
                    startRecording(webSocket)
                }
                Log.d(TAG, "[Service-WebSocket-4-4 완료] 실시간 녹음 시작 완료")
                
                // ConnectionEstablished 이벤트 발생 (ViewModel에서 Firebase 업데이트 처리)
                Log.d(TAG, "[Service-WebSocket-4-5] ConnectionEstablished 이벤트 발행")
                _serviceEvents.emit(ServiceEvent.ConnectionEstablished(lastProcessedChunkId))
                Log.d(TAG, "[Service-WebSocket-4-5 완료] ConnectionEstablished 이벤트 발행 완료")
                Log.d(TAG, "========================================")
                Log.d(TAG, "[Service-WebSocket-4 완료] 연결 확립 처리 완료")
                Log.d(TAG, "========================================")
            } catch (e: Exception) {
                Log.e(TAG, "[Service-WebSocket-4 실패] 연결 확립 처리 중 오류: ${e.message}", e)
            }
        }
    }
    
    /**
     * 재전송이 필요한 청크 파일 목록 조회
     */
    private fun getSavedChunksForRetransmission(lastProcessedChunkId: Long?): List<SavedChunkInfo> {
        return try {
            val dir = packetDir ?: return emptyList()
            if (!dir.exists()) {
                Log.d(TAG, "저장된 청크 디렉토리가 없습니다")
                return emptyList()
            }

            // PCM 파일 목록 조회
            val pcmFiles = dir.listFiles { file ->
                file.extension == "pcm" && file.name.startsWith("chunk_")
            } ?: emptyArray()

            if (pcmFiles.isEmpty()) {
                Log.d(TAG, "저장된 청크 파일이 없습니다")
                return emptyList()
            }

            Log.d(TAG, "로컬에 저장된 총 청크 파일: ${pcmFiles.size}개")

            // 청크 파일명에서 ID 추출하여 리스트 생성
            val savedChunks = pcmFiles.mapNotNull { file ->
                try {
                    // "chunk_123.pcm" → 123
                    val chunkId = file.nameWithoutExtension.substringAfter("chunk_").toLongOrNull()
                    if (chunkId != null) {
                        SavedChunkInfo(chunkId, file)
                    } else {
                        Log.w(TAG, "⚠청크 ID 파싱 실패: ${file.name}")
                        null
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "청크 파일 처리 실패: ${file.name}", e)
                    null
                }
            }.sortedBy { it.chunkId }

            // 서버가 받지 못한 청크만 필터링
            val missingChunks = if (lastProcessedChunkId == null) {
                // 서버가 아무것도 받지 못한 경우 → 모든 청크 재전송
                savedChunks
            } else {
                // lastProcessedChunkId보다 큰 청크만 재전송
                savedChunks.filter { it.chunkId > lastProcessedChunkId }
            }

            if (missingChunks.isNotEmpty()) {
                Log.d(TAG, "재전송 대상 청크:")
                missingChunks.forEach { chunk ->
                    Log.d(TAG, "  - 청크 ID: ${chunk.chunkId}, 파일: ${chunk.file.name}, 크기: ${chunk.file.length()} bytes")
                }
            }

            missingChunks

        } catch (e: Exception) {
            Log.e(TAG, "재전송 청크 조회 실패", e)
            emptyList()
        }
    }
    
    /**
     * 저장된 청크 정보를 담는 데이터 클래스
     */
    private data class SavedChunkInfo(
        val chunkId: Long,
        val file: File
    )
    
    /**
     * 누락된 청크 재전송
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun retransmitMissingChunks(
        webSocket: WebSocket,
        chunks: List<SavedChunkInfo>
    ) {
        Log.d(TAG, "========================================")
        Log.d(TAG, "청크 재전송 시작")
        Log.d(TAG, "총 ${chunks.size}개 청크 재전송 예정")
        Log.d(TAG, "========================================")

        var successCount = 0
        var failCount = 0

        chunks.forEach { chunkInfo ->
            try {
                // 파일에서 오디오 데이터 읽기
                val audioData = chunkInfo.file.readBytes()

                // 메타데이터 파일도 읽기 (있으면)
                val dir = packetDir ?: return@forEach
                val metaFile = File(dir, "chunk_${chunkInfo.chunkId}.json")
                val timestamp = if (metaFile.exists()) {
                    try {
                        val metaJson = JSONObject(metaFile.readText())
                        metaJson.optString("timestamp", getCurrentTimestamp())
                    } catch (e: Exception) {
                        getCurrentTimestamp()
                    }
                } else {
                    getCurrentTimestamp()
                }

                // 메타데이터 생성
                val metaJson = JSONObject().apply {
                    put("type", "AUDIO_CHUNK")
                    put("chunkId", chunkInfo.chunkId)
                    put("encoding", "audio/pcm")
                    put("timestamp", timestamp)
                }

                val metaBytes = metaJson.toString().toByteArray(Charsets.UTF_8)
                val metaLength = metaBytes.size

                // 바이너리 메시지 조립
                val buffer = ByteBuffer.allocate(4 + metaLength + audioData.size)
                buffer.putInt(metaLength)
                buffer.put(metaBytes)
                buffer.put(audioData)

                val totalSize = buffer.position()
                val binaryMessage = buffer.array().toByteString(0, totalSize)

                // WebSocket으로 전송
                val success = webSocket.send(binaryMessage)

                if (success) {
                    successCount++
                    Log.d(TAG, "재전송 성공 - ID: ${chunkInfo.chunkId}, 크기: ${audioData.size} bytes (${successCount}/${chunks.size})")
                } else {
                    failCount++
                    Log.w(TAG, "재전송 실패 - ID: ${chunkInfo.chunkId}")
                    // 큐가 가득 찬 경우 잠시 대기
                    delay(100)
                }

                // 전송 간 짧은 딜레이 (서버 부하 방지)
                delay(10)

            } catch (e: Exception) {
                failCount++
                Log.e(TAG, "청크 재전송 실패 - ID: ${chunkInfo.chunkId}", e)
            }
        }

        Log.d(TAG, "========================================")
        Log.d(TAG, "청크 재전송 완료")
        Log.d(TAG, "성공: ${successCount}개, 실패: ${failCount}개")
        Log.d(TAG, "========================================")
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    private fun startRecording(webSocket: WebSocket) {
        Log.d(TAG, "[Service-Recording-1] 녹음 시작 요청")
        if (isRecording) {
            Log.d(TAG, "[Service-Recording-1 스킵] 이미 녹음 중입니다")
            return
        }
        
        // 권한 체크
        Log.d(TAG, "[Service-Recording-1-1] 오디오 녹음 권한 확인")
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "[Service-Recording-1-1 실패] 오디오 녹음 권한이 없습니다")
            return
        }
        Log.d(TAG, "[Service-Recording-1-1 완료] 오디오 녹음 권한 확인 완료")
        
        try {
            Log.d(TAG, "[Service-Recording-1-2] AudioRecord 초기화 시작: sampleRate=$sampleRate, bufferSize=$bufferSize")
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )
            
            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "[Service-Recording-1-2 실패] AudioRecord 초기화 실패")
                audioRecord?.release()
                audioRecord = null
                return
            }
            Log.d(TAG, "[Service-Recording-1-2 완료] AudioRecord 초기화 성공")
            
            Log.d(TAG, "[Service-Recording-1-3] AudioRecord 녹음 시작")
            audioRecord?.startRecording()
            isRecording = true
            Log.d(TAG, "[Service-Recording-1-3 완료] AudioRecord 녹음 시작 완료")
            
            Log.d(TAG, "[Service-Recording-1-4] 오디오 데이터 읽기 Job 시작")
            recordingJob = serviceScope.launch(Dispatchers.IO) {
                readAndSendAudioData(webSocket)
            }
            Log.d(TAG, "[Service-Recording-1 완료] 녹음 시작 완료")
        } catch (e: Exception) {
            Log.e(TAG, "[Service-Recording-1 실패] 녹음 시작 실패: ${e.message}", e)
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun readAndSendAudioData(webSocket: WebSocket) {
        Log.d(TAG, "[Audio-1] 오디오 데이터 읽기 및 전송 시작")
        val chunkSizeInBytes = frameSize * 2
        val buffer = ByteArray(chunkSizeInBytes)
        var chunkCount = 0L
        var lastLogTime = System.currentTimeMillis()
        val logInterval = 5000L // 5초마다 로그
        
        try {
            while (isRecording &&
                audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING &&
                coroutineContext.isActive) {
                
                val bytesRead = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                
                if (bytesRead > 0) {
                    if (micEnabled) {
                        val audioChunk = buffer.copyOf(bytesRead)
                        sendAudioChunk(webSocket, audioChunk)
                        chunkCount++
                        
                        // 주기적으로 로그 출력 (5초마다)
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastLogTime >= logInterval) {
                            Log.d(TAG, "[Audio-2] 음성 패킷 전송 중: 총 ${chunkCount}개 전송됨 (마지막 5초간)")
                            lastLogTime = currentTime
                        }
                    } else {
                        // 마이크가 꺼져있으면 스킵 (로그는 너무 자주 찍히지 않도록)
                        if (chunkCount % 100 == 0L) {
                            Log.d(TAG, "[Audio-3] 마이크 꺼짐 상태 - 패킷 전송 스킵")
                        }
                    }
                } else if (bytesRead < 0) {
                    Log.w(TAG, "[Audio-경고] 오디오 읽기 실패: bytesRead=$bytesRead")
                }
            }
            Log.d(TAG, "[Audio-1 완료] 오디오 데이터 읽기 및 전송 종료: 총 ${chunkCount}개 전송됨")
        } catch (e: CancellationException) {
            Log.d(TAG, "[Audio-취소] 오디오 읽기 취소됨: 총 ${chunkCount}개 전송됨")
        } catch (e: Exception) {
            Log.e(TAG, "[Audio-에러] 오디오 읽기 에러: ${e.message} (총 ${chunkCount}개 전송됨)", e)
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendAudioChunk(webSocket: WebSocket, audioData: ByteArray) {
        try {
            val chunkId = chunkIdCounter.getAndIncrement()
            
            val metaJson = JSONObject().apply {
                put("type", "AUDIO_CHUNK")
                put("chunkId", chunkId)
                put("encoding", "audio/pcm")
                put("timestamp", DateTimeUtils.koreaToUtcTime(getCurrentTimestamp()))
            }
            
            val metaBytes = metaJson.toString().toByteArray(Charsets.UTF_8)
            val metaLength = metaBytes.size
            
            val buffer = ByteBuffer.allocate(4 + metaLength + audioData.size)
            buffer.putInt(metaLength)
            buffer.put(metaBytes)
            buffer.put(audioData)
            
            val totalSize = buffer.position()
            val binaryMessage = buffer.array().toByteString(0, totalSize)
            
            // WebSocket으로 전송
            val sendSuccess = webSocket.send(binaryMessage)
            if (!sendSuccess) {
                Log.w(TAG, "[Audio-Chunk-경고] 청크 전송 실패: chunkId=$chunkId, size=${audioData.size} bytes (큐가 가득 참)")
            }
            
            // 로컬 파일로 저장
            packetDir?.let { dir ->
                try {
                    val audioFile = File(dir, "chunk_${chunkId}.pcm")
                    FileOutputStream(audioFile).use { it.write(audioData) }
                } catch (e: Exception) {
                    Log.e(TAG, "[Audio-Chunk-저장실패] 청크 파일 저장 실패: chunkId=$chunkId, ${e.message}", e)
                }
            }
            
            // 첫 번째 청크와 주기적으로 로그 출력 (100개마다)
            if (chunkId == 0L || chunkId % 100 == 0L) {
                Log.d(TAG, "[Audio-Chunk] 청크 전송: chunkId=$chunkId, size=${audioData.size} bytes, totalSize=$totalSize bytes, success=$sendSuccess")
            }
        } catch (e: Exception) {
            Log.e(TAG, "[Audio-Chunk-예외] 청크 전송 예외: ${e.message}", e)
        }
    }
    
    private fun stopRecording() {
        if (!isRecording) return
        
        isRecording = false
        recordingJob?.cancel()
        recordingJob = null
        
        try {
            audioRecord?.apply {
                if (recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    stop()
                }
                release()
            }
            audioRecord = null
            Log.d(TAG, "녹음 중지 완료")
        } catch (e: Exception) {
            Log.e(TAG, "녹음 중지 중 에러 발생", e)
        }
    }
    
    private fun disconnectSseConnection() {
        // SSE 재연결 Job 취소
        sseReconnectJob?.cancel()
        sseReconnectJob = null
        
        // SSE 연결 해제
        eventSource?.cancel()
        eventSource = null
        
        Log.d(TAG, "SSE 연결 해제 완료")
    }
    
    /**
     * 청크 파일 디렉토리 삭제
     */
    private fun deleteChunkFiles() {
        try {
            packetDir?.let { dir ->
                if (dir.exists()) {
                    val deletedCount = dir.listFiles()?.count { it.delete() } ?: 0
                    if (dir.delete()) {
                        Log.d(TAG, "청크 파일 디렉토리 삭제 완료: ${dir.absolutePath} (파일 ${deletedCount}개)")
                    } else {
                        Log.w(TAG, "청크 파일 디렉토리 삭제 실패: ${dir.absolutePath}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "청크 파일 디렉토리 삭제 중 에러 발생", e)
        }
    }
    
    private fun stopConnections() {
        stopRecording()
        disconnectSseConnection()
        
        webSocket?.close(1000, "서비스 종료")
        webSocket = null
        
        sseServerUrl = null
        sseMeetingId = null
        sseJwtToken = null
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCurrentTimestamp(): String {
        return try {
            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        } catch (e: Exception) {
            System.currentTimeMillis().toString()
        }
    }
    
    private fun String?.toSocketResponseType(): SocketResponseType? {
        return try {
            SocketResponseType.valueOf(this ?: "")
        } catch (e: IllegalArgumentException) {
            null
        }
    }
    
    private fun String?.toSseResponseType(): SseResponseType? {
        return try {
            SseResponseType.valueOf(this ?: "")
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}

