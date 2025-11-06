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
        when (intent?.action) {
            ACTION_START -> {
                val serverUrl = intent.getStringExtra(EXTRA_SERVER_URL)
                val meetingId = intent.getLongExtra(EXTRA_MEETING_ID, -1L)
                val jwtToken = intent.getStringExtra(EXTRA_JWT_TOKEN)
                
                if (serverUrl != null && meetingId != -1L && jwtToken != null) {
                    startForeground(NOTIFICATION_ID, createNotification())
                    startConnections(serverUrl, meetingId, jwtToken)
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
        currentMeetingId = meetingId
        packetDir = File(cacheDir, "audio_packets/$meetingId").apply {
            if (!exists()) mkdirs()
        }
        
        serviceScope.launch {
            // WebSocket 연결
            connectWebSocket(serverUrl, meetingId, jwtToken)
            
            // SSE 연결
            connectSse(serverUrl, meetingId, jwtToken)
        }
    }
    
    private fun connectWebSocket(serverUrl: String, meetingId: Long, jwtToken: String) {
        serviceScope.launch {
            try {
                Log.d(TAG, "WebSocket 연결 시작")
                val wsUrl = "${serverUrl}ws/v1/meeting/$meetingId/audio"
                
                val request = Request.Builder()
                    .url(wsUrl)
                    .addHeader("Authorization", "Bearer $jwtToken")
                    .build()
                
                webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        Log.d(TAG, "WebSocket 연결 성공!")
                    }
                    
                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onMessage(webSocket: WebSocket, text: String) {
                        parseWebSocketMessage(text, webSocket)
                    }
                    
                    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                        Log.e(TAG, "WebSocket 연결 실패: ${t.message}", t)
                    }
                })
            } catch (e: Exception) {
                Log.e(TAG, "WebSocket 연결 예외: ${e.message}", e)
            }
        }
    }
    
    private fun connectSse(serverUrl: String, meetingId: Long, jwtToken: String) {
        sseServerUrl = serverUrl
        sseMeetingId = meetingId
        sseJwtToken = jwtToken
        
        sseReconnectJob?.cancel()
        
        serviceScope.launch {
            try {
                Log.d(TAG, "SSE 연결 시작")
                val sseUrl = "${serverUrl}api/v1/meetings/$meetingId/events/subscribe"
                
                val request = Request.Builder()
                    .url(sseUrl)
                    .addHeader("Authorization", "Bearer $jwtToken")
                    .addHeader("Accept", "text/event-stream")
                    .build()
                
                val sseClient = OkHttpClient.Builder()
                    .readTimeout(0, TimeUnit.SECONDS)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .build()
                
                eventSource = EventSources.createFactory(sseClient)
                    .newEventSource(request, object : EventSourceListener() {
                        override fun onOpen(eventSource: EventSource, response: Response) {
                            Log.d(TAG, "SSE 연결 성공!")
                        }
                        
                        @RequiresApi(Build.VERSION_CODES.O)
                        override fun onEvent(
                            eventSource: EventSource,
                            id: String?,
                            type: String?,
                            data: String
                        ) {
                            parseSseEvent(type, data)
                        }
                        
                        override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                            Log.e(TAG, "SSE 실패: ${t?.message}", t)
                        }
                    })
                
                startSseReconnectJob()
            } catch (e: Exception) {
                Log.e(TAG, "SSE 예외: ${e.message}", e)
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
        Log.d(TAG, "연결 확립됨")
        Log.d(TAG, "서버 마지막 처리 청크 ID: ${lastProcessedChunkId ?: "없음 (첫 연결)"}")
        
        serviceScope.launch(Dispatchers.IO) {
            try {
                // 재전송이 필요한 청크 확인 및 재전송
                val savedChunks = getSavedChunksForRetransmission(lastProcessedChunkId)
                
                if (savedChunks.isNotEmpty()) {
                    Log.d(TAG, "재전송 필요한 청크: ${savedChunks.size}개")
                    retransmitMissingChunks(webSocket, savedChunks)
                    
                    // 청크 ID 카운터를 재전송한 마지막 청크 다음으로 설정
                    val lastRetransmittedId = savedChunks.maxOfOrNull { it.chunkId } ?: -1
                    chunkIdCounter.set(lastRetransmittedId + 1)
                    Log.d(TAG, "청크 ID 카운터를 ${lastRetransmittedId + 1}로 설정")
                } else {
                    Log.d(TAG, "재전송 필요한 청크 없음")
                    
                    // 청크 ID 카운터 초기화
                    if (lastProcessedChunkId != null) {
                        chunkIdCounter.set(lastProcessedChunkId + 1)
                        Log.d(TAG, "청크 ID를 ${lastProcessedChunkId + 1}부터 시작합니다")
                    } else {
                        chunkIdCounter.set(0)
                        Log.d(TAG, "청크 ID를 0부터 시작합니다")
                    }
                }
                
                // 실시간 녹음 시작
                withContext(Dispatchers.Main) {
                    startRecording(webSocket)
                }
                
                // ConnectionEstablished 이벤트 발생 (ViewModel에서 Firebase 업데이트 처리)
                _serviceEvents.emit(ServiceEvent.ConnectionEstablished(lastProcessedChunkId))
            } catch (e: Exception) {
                Log.e(TAG, "연결 확립 처리 중 오류", e)
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
        if (isRecording) {
            return
        }
        
        // 권한 체크
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "오디오 녹음 권한이 없습니다")
            return
        }
        
        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )
            
            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord 초기화 실패")
                audioRecord?.release()
                audioRecord = null
                return
            }
            
            audioRecord?.startRecording()
            isRecording = true
            
            recordingJob = serviceScope.launch(Dispatchers.IO) {
                readAndSendAudioData(webSocket)
            }
        } catch (e: Exception) {
            Log.e(TAG, "녹음 시작 실패: ${e.message}", e)
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun readAndSendAudioData(webSocket: WebSocket) {
        val chunkSizeInBytes = frameSize * 2
        val buffer = ByteArray(chunkSizeInBytes)
        
        try {
            while (isRecording &&
                audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING &&
                coroutineContext.isActive) {
                
                val bytesRead = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                
                if (bytesRead > 0 && micEnabled) {
                    val audioChunk = buffer.copyOf(bytesRead)
                    sendAudioChunk(webSocket, audioChunk)
                }
            }
        } catch (e: CancellationException) {
            Log.d(TAG, "오디오 읽기 취소됨")
        } catch (e: Exception) {
            Log.e(TAG, "오디오 읽기 에러", e)
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
                put("timestamp", getCurrentTimestamp())
            }
            
            val metaBytes = metaJson.toString().toByteArray(Charsets.UTF_8)
            val metaLength = metaBytes.size
            
            val buffer = ByteBuffer.allocate(4 + metaLength + audioData.size)
            buffer.putInt(metaLength)
            buffer.put(metaBytes)
            buffer.put(audioData)
            
            val totalSize = buffer.position()
            val binaryMessage = buffer.array().toByteString(0, totalSize)
            
            webSocket.send(binaryMessage)
            
            // 로컬 파일로 저장
            packetDir?.let { dir ->
                val audioFile = File(dir, "chunk_${chunkId}.pcm")
                FileOutputStream(audioFile).use { it.write(audioData) }
            }
        } catch (e: Exception) {
            Log.e(TAG, "청크 전송 예외: ${e.message}", e)
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

