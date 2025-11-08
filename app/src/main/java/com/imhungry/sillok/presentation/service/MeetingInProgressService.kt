package com.imhungry.sillok.presentation.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.imhungry.sillok.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.WebSocket
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicLong

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
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _serviceEvents = MutableSharedFlow<ServiceEvent>()
    
    // 청크 ID 카운터
    private val chunkIdCounter = AtomicLong(0)
    
    private var currentMeetingId: Long = 1L
    private var packetDir: File? = null
    
    // 분리된 매니저 클래스들
    private lateinit var webSocketManager: WebSocketManager
    private lateinit var sseManager: SseManager
    private lateinit var audioRecorder: AudioRecorder
    private lateinit var chunkRetransmitter: ChunkRetransmitter
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannel()
        
        // 매니저 클래스 초기화
        webSocketManager = WebSocketManager(
            serviceScope = serviceScope,
            serviceEvents = _serviceEvents,
            onConnectionEstablished = { lastProcessedChunkId, webSocket ->
                handleConnectionEstablished(lastProcessedChunkId, webSocket)
            },
            onMeetingCompleted = {
                if (::audioRecorder.isInitialized) {
                    audioRecorder.stopRecording()
                }
                if (::sseManager.isInitialized) {
                    sseManager.disconnect()
                }
                if (::chunkRetransmitter.isInitialized) {
                    chunkRetransmitter.deleteChunkFiles()
                }
            }
        )
        sseManager = SseManager(serviceScope, _serviceEvents)
        
        // Service 이벤트 관찰 (MeetingCompleted 이벤트 처리)
        observeServiceEvents()
        
        Log.d(TAG, "Service 생성됨")
    }
    
    private fun observeServiceEvents() {
        serviceScope.launch {
            serviceEvents.collectLatest { event ->
                when (event) {
                    is ServiceEvent.MeetingCompleted -> {
                        Log.d(TAG, "MeetingCompleted 이벤트 수신 - WebSocket 연결 종료 및 Service 종료")
                        // WebSocket 연결 종료
                        webSocketManager.close()
                        // Service 종료
                        stopForeground(true)
                        stopSelf()
                    }
                    else -> {
                        // 다른 이벤트는 처리하지 않음
                    }
                }
            }
        }
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
                if (::audioRecorder.isInitialized) {
                    val wasEnabled = audioRecorder.micEnabled
                    audioRecorder.toggleMic()
                    
                    // 마이크를 켤 때 (꺼져있었다가 켜질 때) 이벤트 발행
                    if (!wasEnabled && audioRecorder.micEnabled) {
                        serviceScope.launch {
                            // 마이크가 켜지는 동안 짧은 딜레이 (로딩 표시용)
                            delay(300)
                            _serviceEvents.emit(ServiceEvent.MicEnabled)
                        }
                    }
                }
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
        
        // 매니저 클래스 초기화
        chunkRetransmitter = ChunkRetransmitter(
            packetDir = packetDir,
            getCurrentTimestamp = { getCurrentTimestamp() }
        )
        audioRecorder = AudioRecorder(
            context = this,
            serviceScope = serviceScope,
            packetDir = packetDir,
            chunkIdCounter = chunkIdCounter
        )
        
        serviceScope.launch {
            // WebSocket 연결
            Log.d(TAG, "[Service-1-3-3] WebSocket 연결 시작")
            webSocketManager.connect(serverUrl, meetingId, jwtToken)
            
            // SSE 연결
            Log.d(TAG, "[Service-1-3-4] SSE 연결 시작")
            sseManager.connect(serverUrl, meetingId, jwtToken)
            Log.d(TAG, "[Service-1-3 완료] startConnections 완료")
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
                val savedChunks = if (::chunkRetransmitter.isInitialized) {
                    chunkRetransmitter.getSavedChunksForRetransmission(lastProcessedChunkId)
                } else {
                    emptyList()
                }
                Log.d(TAG, "[Service-WebSocket-4-1 완료] 재전송 필요한 청크: ${savedChunks.size}개")
                
                // 재전송이 필요한 경우 먼저 재전송 완료 후 녹음 시작
                if (savedChunks.isNotEmpty()) {
                    Log.d(TAG, "[Service-WebSocket-4-2] 청크 재전송 시작 (녹음 시작 전)")
                    chunkRetransmitter.retransmitMissingChunks(webSocket, savedChunks)
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
                    if (::audioRecorder.isInitialized) {
                        audioRecorder.startRecording(webSocket)
                    }
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
    
    private fun stopConnections() {
        if (::audioRecorder.isInitialized) {
            audioRecorder.stopRecording()
        }
        if (::sseManager.isInitialized) {
            sseManager.disconnect()
        }
        if (::webSocketManager.isInitialized) {
            webSocketManager.close()
        }
        if (::chunkRetransmitter.isInitialized) {
            chunkRetransmitter.deleteChunkFiles()
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCurrentTimestamp(): String {
        return try {
            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        } catch (e: Exception) {
            System.currentTimeMillis().toString()
        }
    }
}

