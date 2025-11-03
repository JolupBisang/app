package com.imhungry.sillok.presentation.viewmodel.meeting

import android.app.Application
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.imhungry.sillok.data.local.UserStore
import com.imhungry.sillok.data.model.realtime.ErrorResponse
import com.imhungry.sillok.data.model.realtime.LiveFeedbackDto
import com.imhungry.sillok.data.model.realtime.LiveSummaryDto
import com.imhungry.sillok.data.model.realtime.RealtimeSegmentDto
import com.imhungry.sillok.data.model.realtime.SocketResponse
import com.imhungry.sillok.data.model.realtime.SocketResponseType
import com.imhungry.sillok.data.model.realtime.SseResponseType
import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.model.participation.UserParticipationRate
import com.imhungry.sillok.domain.usecase.agenda.GetAgendasUseCase
import com.imhungry.sillok.domain.usecase.feedback.GetFeedbacksUseCase
import com.imhungry.sillok.domain.usecase.participation.GetParticipationRateHistoryUseCase
import com.imhungry.sillok.domain.usecase.segment.GetSegmentsUseCase
import com.imhungry.sillok.domain.usecase.summary.GetSummariesUseCase
import com.imhungry.sillok.presentation.state.meeting.FeedbackUi
import com.imhungry.sillok.presentation.state.meeting.MeetingInProgressState
import com.imhungry.sillok.presentation.state.meeting.SegmentUi
import com.imhungry.sillok.presentation.state.meeting.SummaryUi
import com.imhungry.sillok.presentation.util.DateTimeUtils
import com.imhungry.sillok.presentation.util.ProfileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class MeetingInProgressViewModel @Inject constructor(
    private val getAgendasUseCase: GetAgendasUseCase,
    private val getSegmentsUseCase: GetSegmentsUseCase,
    private val getSummariesUseCase: GetSummariesUseCase,
    private val getParticipationRateHistoryUseCase: GetParticipationRateHistoryUseCase,
    private val getFeedbacksUseCase: GetFeedbacksUseCase,
    private val userStore: UserStore,
    private val app: Application,
) : AndroidViewModel(app) {
    companion object {
        private const val TAG = "MeetingInProgressViewModel"
    }

    private val _state = MutableStateFlow(MeetingInProgressState())
    val state: StateFlow<MeetingInProgressState> = _state.asStateFlow()

    private val _micEnabled = MutableStateFlow(true)
    val micEnabled: StateFlow<Boolean> = _micEnabled.asStateFlow()

    private val gson = Gson()
    private var webSocket: WebSocket? = null
    private var eventSource: EventSource? = null

    // AudioRecord 관련
    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private var isRecording = false

    private val sampleRate = 16000
    private val frameSize = 16000
    private val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    ).coerceAtLeast(frameSize)

    // 청크 ID 카운터
    private val chunkIdCounter = AtomicLong(0)

    // ✅ 회의 ID 저장
    private var currentMeetingId: Long = 1L

    // ✅ 오디오 패킷 저장 디렉토리 (lazy 초기화)
    private val packetDir: File by lazy {
        File(getApplication<Application>().cacheDir, "audio_packets/$currentMeetingId").apply {
            if (!exists()) {
                mkdirs()
                Log.d(TAG, "📁 오디오 패킷 디렉토리 생성: ${absolutePath}")
            } else {
                Log.d(TAG, "📁 오디오 패킷 디렉토리 존재: ${absolutePath}")
            }
        }
    }

    // LiveData
    private val _recordingStatus = MutableLiveData<String>()
    val recordingStatus: LiveData<String> = _recordingStatus

    private val _connectionStatus = MutableLiveData<String>()
    val connectionStatus: LiveData<String> = _connectionStatus

    private val okHttpClient = OkHttpClient.Builder()
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()

    @RequiresApi(Build.VERSION_CODES.O)
    fun initialize(meetingId: Long) {
        _state.update { it.copy(meetingId = meetingId) }
        refreshAll()
        testWebSocketConnection(
            "ws://192.168.68.103:8080",
            1L,
            "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJqb2V1bmd5ZW9uZzIzQGdtYWlsLmNvbSIsIm5pY2tuYW1lIjoi7KGw7J2A6rK9IiwidXNlcklkIjoxLCJpc3MiOiJTaWxyb2siLCJpYXQiOjE3NjE2NTk4MzgsImV4cCI6MTc3MDI5OTgzOH0.X2uiwGM6JhlU1BFsA-VZUSbJ4e191lLKxIdp97_0Z4SeqzNGXnlLpyvbmgb5SsWu"
        )
        testSseConnection(
            "http://192.168.68.103:8080",
            1L,
            "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJqb2V1bmd5ZW9uZzIzQGdtYWlsLmNvbSIsIm5pY2tuYW1lIjoi7KGw7J2A6rK9IiwidXNlcklkIjoxLCJpc3MiOiJTaWxyb2siLCJpYXQiOjE3NjE2NTk4MzgsImV4cCI6MTc3MDI5OTgzOH0.X2uiwGM6JhlU1BFsA-VZUSbJ4e191lLKxIdp97_0Z4SeqzNGXnlLpyvbmgb5SsWu"
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshAll() {
		viewModelScope.launch {
			_state.update { it.copy(isLoading = true) }
            val meetingId = state.value.meetingId

            val agendasDeferred = async { getAgendasUseCase(meetingId) }

            var startMillis: Long? = null

            try {
                val snapshot = FirebaseFirestore.getInstance()
                    .collection("meetings")
                    .document(meetingId.toString())
                    .get()
                    .await()
                startMillis = snapshot.getLong("startMillis")
                if (startMillis != null) {
                    _state.update { it.copy(startTime = startMillis) }
                }
            } catch (e: Exception) {
				Log.e(TAG, "startMillis 조회 실패: ${e.message}", e)
            }

            when (val result = agendasDeferred.await()) {
				is ApiResult.Success -> _state.update { it.copy(agendas = result.data) }
				is ApiResult.Failure -> Log.e(TAG, "아젠다 로드 실패: ${result.message}")
            }

            val currentUserId = userStore.user.first()?.id

            if (startMillis != null) {
                val segmentsDeferred = async { getSegmentsUseCase(meetingId) }
                val summariesDeferred = async { getSummariesUseCase(meetingId) }
                val participationDeferred = async { getParticipationRateHistoryUseCase(meetingId) }
                val feedbacksDeferred = async { getFeedbacksUseCase(meetingId) }

				when (val result = segmentsDeferred.await()) {
                    is ApiResult.Success -> {
                        val ui = result.data.mapIndexed { index, seg ->
                            val prevUserId = if (index > 0) result.data[index - 1].userId else null
                            val nextUserId = if (index < result.data.lastIndex) result.data[index + 1].userId else null
                            val isSameAsPrevious = prevUserId != null && prevUserId == seg.userId
                            val isSameAsNext = nextUserId != null && nextUserId == seg.userId
                            SegmentUi(
                                timestamp = DateTimeUtils.getElapsedString(startMillis, seg.timestamp),
                                text = seg.text,
                                nickname = seg.userName,
                                profileImage = ProfileUtils.getProfileDrawableForUser(seg.userId),
                                isFromCurrentUser = currentUserId != null && seg.userId == currentUserId,
                                isSameAsPrevious = isSameAsPrevious,
                                isSameAsNext = isSameAsNext
                            )
                        }
                        _state.update { it.copy(segments = ui) }
                    }
					is ApiResult.Failure -> Log.e(TAG, "세그먼트 로드 실패: ${result.message}")
                }

				when (val result = summariesDeferred.await()) {
                    is ApiResult.Success -> {
                        val ui = result.data.map {
                            SummaryUi(
                                content = it.content,
                                timestamp = DateTimeUtils.getElapsedString(startMillis, it.timestamp)
                            )
                        }
                        _state.update { it.copy(summaries = ui) }
                    }
					is ApiResult.Failure -> Log.e(TAG, "요약 로드 실패: ${result.message}")
                }

				when (val result = participationDeferred.await()) {
                    is ApiResult.Success -> {
                        val sorted = result.data.sortedByDescending { it.rate }
                        _state.update { it.copy(participationRates = sorted) }
                    }
					is ApiResult.Failure -> Log.e(TAG, "참여율 로드 실패: ${result.message}")
                }

				when (val result = feedbacksDeferred.await()) {
                    is ApiResult.Success -> {
                        val ui = result.data.map {
                            FeedbackUi(
                                comment = it.comment,
                                timestamp = DateTimeUtils.getElapsedString(startMillis, it.timestamp),
                                isRead = false
                            )
                        }
                        _state.update { it.copy(feedbacks = ui) }
                    }
					is ApiResult.Failure -> Log.e(TAG, "피드백 로드 실패: ${result.message}")
                }
            }

			_state.update { it.copy(isLoading = false) }
			//startRealtimeService(meetingId)
		}
	}

    fun markFeedbackReadAt(index: Int) {
        // 특정 인덱스의 피드백을 읽음 처리합니다.
        val current = state.value.feedbacks
        if (index !in current.indices) return
        val updated = current.toMutableList()
        updated[index] = updated[index].copy(isRead = true)
        _state.update { it.copy(feedbacks = updated) }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun setMicEnabled(enabled: Boolean) {
        _micEnabled.value = enabled
    }

    fun toggleMic() {
        _micEnabled.value = !_micEnabled.value
    }

    fun changeAgendaStatus(agendaId: Long, isCompleted: Boolean) {
        val previous = state.value.agendas
        val updated = previous.map { agenda ->
            if (agenda.agendaId == agendaId) agenda.copy(isCompleted = isCompleted) else agenda
        }
        _state.update { it.copy(agendas = updated) }

//        viewModelScope.launch {
//            when (val result = changeAgendaStatusUseCase(meetingId, agendaId, isCompleted)) {
//                is ApiResult.Success -> {}
//                is ApiResult.Failure -> {
//                    _state.update { it.copy(agendas = previous, error = result.message) }
//                }
//            }
//        }
    }

    // ========================================
    // WebSocket 연결 테스트
    // ========================================

    /**
     * WebSocket 연결 테스트
     * @param serverUrl 서버 URL (예: "ws://10.0.2.2:8080")
     * @param meetingId 회의 ID
     * @param jwtToken JWT 인증 토큰
     */
    fun testWebSocketConnection(
        serverUrl: String,
        meetingId: Long,
        jwtToken: String
    ) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "========================================")
                Log.d(TAG, "WebSocket 연결 테스트 시작")
                Log.d(TAG, "========================================")

                val wsUrl = "$serverUrl/ws/v1/meeting/$meetingId/audio"

                val request = Request.Builder()
                    .url(wsUrl)
                    .addHeader("Authorization", "Bearer $jwtToken")
                    .build()

                webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        Log.d(TAG, "✅ WebSocket 연결 성공!")
                        Log.d(TAG, "응답 코드: ${response.code}")
                    }

                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onMessage(webSocket: WebSocket, text: String) {
                        Log.d(TAG, "========================================")
                        Log.d(TAG, "📨 [WebSocket] 텍스트 메시지 수신")
                        Log.d(TAG, "원본: $text")
                        parseWebSocketMessage(text, webSocket)
                        Log.d(TAG, "========================================")
                    }

                    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                        Log.e(TAG, "❌ [WebSocket] 연결 실패: ${t.message}", t)
                    }
                })

            } catch (e: Exception) {
                Log.e(TAG, "❌ [WebSocket] 연결 예외: ${e.message}", e)
            }
        }
    }

    /**
     * WebSocket 메시지 파싱
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun parseWebSocketMessage(jsonString: String, webSocket: WebSocket) {
        try {
            val jsonObject = JsonParser.parseString(jsonString).asJsonObject
            val typeString = jsonObject.get("type")?.asString

            val socketType = typeString.toSocketResponseType()
            when (socketType) {
                // 1. 연결 확립 (최초 1회)
                SocketResponseType.CONNECTION_ESTABLISHED -> {
                    val response = gson.fromJson<SocketResponse<Long>>(
                        jsonString,
                        object : TypeToken<SocketResponse<Long>>() {}.type
                    )
                    handleConnectionEstablished(response.data, webSocket)
                }

                // 2. 실시간 음성→텍스트 (가장 자주 발생)
                SocketResponseType.DIARIZED_SEGMENT -> {
                    val response = gson.fromJson<SocketResponse<RealtimeSegmentDto>>(
                        jsonString,
                        object : TypeToken<SocketResponse<RealtimeSegmentDto>>() {}.type
                    )
                    handleDiarizedSegment(response.data)
                }

                // 3. 회의록 생성 완료
                SocketResponseType.MEETING_NOTE_CREATED -> {
                    val response = gson.fromJson<SocketResponse<String>>(
                        jsonString,
                        object : TypeToken<SocketResponse<String>>() {}.type
                    )
                    handleMeetingNoteCreated(response.data)
                }

                // 4. 에러
                SocketResponseType.ERROR -> {
                    val response = gson.fromJson<SocketResponse<ErrorResponse>>(
                        jsonString,
                        object : TypeToken<SocketResponse<ErrorResponse>>() {}.type
                    )
                    handleError(response.data)
                }

                else -> {
                    Log.w(TAG, "⚠️ 알 수 없는 메시지 타입: $typeString")
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ 메시지 파싱 실패: ${e.message}", e)
            Log.e(TAG, "원본 메시지: $jsonString")
        }
    }

    /**
     * CONNECTION_ESTABLISHED 수신 시 처리
     * 1. 로컬 저장 청크와 서버 청크 비교
     * 2. 누락된 청크 재전송
     * 3. 실시간 녹음 시작
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleConnectionEstablished(lastProcessedChunkId: Long?, webSocket: WebSocket) {
        Log.d(TAG, "========================================")
        Log.d(TAG, "✅ 연결 확립됨")
        Log.d(TAG, "서버 마지막 처리 청크 ID: ${lastProcessedChunkId ?: "없음 (첫 연결)"}")
        Log.d(TAG, "========================================")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1️⃣ 로컬에 저장된 청크 확인
                val savedChunks = getSavedChunksForRetransmission(lastProcessedChunkId)

                if (savedChunks.isNotEmpty()) {
                    Log.d(TAG, "📦 재전송 필요한 청크: ${savedChunks.size}개")

                    // 2️⃣ 누락된 청크 재전송
                    retransmitMissingChunks(webSocket, savedChunks, lastProcessedChunkId)

                    // 청크 ID 카운터를 재전송한 마지막 청크 다음으로 설정
                    val lastRetransmittedId = savedChunks.maxOfOrNull { it.chunkId } ?: -1
                    chunkIdCounter.set(lastRetransmittedId + 1)
                    Log.d(TAG, "청크 ID 카운터를 ${lastRetransmittedId + 1}로 설정")
                } else {
                    Log.d(TAG, "✅ 재전송 필요한 청크 없음")

                    // 청크 ID 카운터 초기화
                    if (lastProcessedChunkId != null) {
                        chunkIdCounter.set(lastProcessedChunkId + 1)
                        Log.d(TAG, "청크 ID를 ${lastProcessedChunkId + 1}부터 시작합니다")
                    } else {
                        chunkIdCounter.set(0)
                        Log.d(TAG, "청크 ID를 0부터 시작합니다")
                    }
                }

                // 3️⃣ 실시간 녹음 시작
                withContext(Dispatchers.Main) {
                    Log.d(TAG, "========================================")
                    Log.d(TAG, "🎙️ 실시간 녹음 시작")
                    Log.d(TAG, "========================================")
                    startRecording(webSocket)
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ 연결 확립 처리 중 오류", e)
                withContext(Dispatchers.Main) {
                    // 오류가 있어도 녹음은 시작
                    startRecording(webSocket)
                }
            }
        }
    }

    // ========================================
    // AudioRecord 직접 사용한 녹음
    // ========================================

    /**
     * AudioRecord를 사용하여 녹음 시작
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun startRecording(webSocket: WebSocket) {
        if (isRecording) {
            Log.w(TAG, "⚠️ 이미 녹음 중입니다")
            return
        }

        try {
            Log.d(TAG, "========================================")
            Log.d(TAG, "🎙️ 오디오 녹음 준비 중...")
            Log.d(TAG, "========================================")
            Log.d(TAG, "샘플레이트: $sampleRate Hz")
            Log.d(TAG, "프레임 크기: $frameSize samples")
            Log.d(TAG, "버퍼 크기: $bufferSize bytes")

            // AudioRecord 생성
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                val errorMsg = "AudioRecord 초기화 실패. State: ${audioRecord?.state}"
                Log.e(TAG, errorMsg)
                _recordingStatus.postValue("녹음 초기화 실패")
                audioRecord?.release()
                audioRecord = null
                return
            }

            // 녹음 시작
            audioRecord?.startRecording()
            isRecording = true

            Log.d(TAG, "✅ 녹음 시작됨")
            Log.d(TAG, "- 샘플레이트: $sampleRate Hz")
            Log.d(TAG, "- 채널: MONO")
            Log.d(TAG, "- 포맷: PCM 16bit")
            Log.d(TAG, "- 청크 크기: ${frameSize * 2} bytes (~${frameSize / sampleRate}초)")
            Log.d(TAG, "========================================")

            _recordingStatus.postValue("녹음 중...")

            // 코루틴으로 오디오 데이터 읽기
            recordingJob = viewModelScope.launch(Dispatchers.IO) {
                readAndSendAudioData(webSocket)
            }

        } catch (e: SecurityException) {
            val errorMsg = "오디오 녹음 권한이 필요합니다"
            Log.e(TAG, "❌ $errorMsg", e)
            _recordingStatus.postValue(errorMsg)
        } catch (e: Exception) {
            val errorMsg = "녹음 시작 실패: ${e.message}"
            Log.e(TAG, "❌ $errorMsg", e)
            _recordingStatus.postValue(errorMsg)
        }
    }
    /**
     * 오디오 데이터를 읽고 WebSocket으로 전송
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun readAndSendAudioData(webSocket: WebSocket) {
        val chunkSizeInBytes = frameSize * 2
        val buffer = ByteArray(chunkSizeInBytes)
        var totalBytesRead = 0L
        var chunkCount = 0

        Log.d(TAG, "📖 오디오 데이터 읽기 시작...")

        try {
            while (isRecording &&
                audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING &&
                isActive) {  // 코루틴이 취소되지 않았는지 확인

                // 오디오 데이터 읽기
                val bytesRead = audioRecord?.read(buffer, 0, buffer.size) ?: 0

                when {
                    bytesRead > 0 -> {
                        totalBytesRead += bytesRead
                        chunkCount++

                        // 읽은 데이터를 복사 (버퍼 재사용을 위해)
                        val audioChunk = buffer.copyOf(bytesRead)

                        // WebSocket으로 전송
                        sendAudioChunk(webSocket, audioChunk)

                        Log.v(TAG, "📤 청크 #$chunkCount 읽음: $bytesRead bytes (총: $totalBytesRead bytes)")

                        // UI 업데이트
                        withContext(Dispatchers.Main) {
                            _recordingStatus.postValue("녹음 중... (청크 #$chunkCount)")
                        }
                    }
                    bytesRead == AudioRecord.ERROR_INVALID_OPERATION -> {
                        Log.e(TAG, "❌ AudioRecord가 제대로 초기화되지 않았습니다")
                        break
                    }

                    bytesRead == AudioRecord.ERROR_BAD_VALUE -> {
                        Log.e(TAG, "❌ 잘못된 파라미터")
                        break
                    }

                    else -> {
                        Log.w(TAG, "⚠️ 예상치 못한 읽기 결과: $bytesRead")
                    }
                }
            }
        } catch (e: CancellationException) {
            Log.d(TAG, "오디오 읽기 취소됨")
            throw e  // 코루틴 취소는 다시 throw
        } catch (e: Exception) {
            Log.e(TAG, "❌ 오디오 읽기 에러", e)
            withContext(Dispatchers.Main) {
                _recordingStatus.postValue("녹음 에러: ${e.message}")
            }
        } finally {
            Log.d(TAG, "📖 오디오 읽기 종료")
            Log.d(TAG, "총 읽은 데이터: $totalBytesRead bytes ($chunkCount 청크)")
        }
    }

    /**
     * ✅ 재전송이 필요한 청크 파일 목록 조회
     */
    private fun getSavedChunksForRetransmission(lastProcessedChunkId: Long?): List<SavedChunkInfo> {
        return try {
            if (!packetDir.exists()) {
                Log.d(TAG, "📁 저장된 청크 디렉토리가 없습니다")
                return emptyList()
            }

            // PCM 파일 목록 조회
            val pcmFiles = packetDir.listFiles { file ->
                file.extension == "pcm" && file.name.startsWith("chunk_")
            } ?: emptyArray()

            if (pcmFiles.isEmpty()) {
                Log.d(TAG, "📁 저장된 청크 파일이 없습니다")
                return emptyList()
            }

            Log.d(TAG, "📁 로컬에 저장된 총 청크 파일: ${pcmFiles.size}개")

            // 청크 파일명에서 ID 추출하여 리스트 생성
            val savedChunks = pcmFiles.mapNotNull { file ->
                try {
                    // "chunk_123.pcm" → 123
                    val chunkId = file.nameWithoutExtension.substringAfter("chunk_").toLongOrNull()
                    if (chunkId != null) {
                        SavedChunkInfo(chunkId, file)
                    } else {
                        Log.w(TAG, "⚠️ 청크 ID 파싱 실패: ${file.name}")
                        null
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "⚠️ 청크 파일 처리 실패: ${file.name}", e)
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
                Log.d(TAG, "📦 재전송 대상 청크:")
                missingChunks.forEach { chunk ->
                    Log.d(TAG, "  - 청크 ID: ${chunk.chunkId}, 파일: ${chunk.file.name}, 크기: ${chunk.file.length()} bytes")
                }
            }

            missingChunks

        } catch (e: Exception) {
            Log.e(TAG, "❌ 재전송 청크 조회 실패", e)
            emptyList()
        }
    }

    /**
     * ✅ 누락된 청크 재전송
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun retransmitMissingChunks(
        webSocket: WebSocket,
        chunks: List<SavedChunkInfo>,
        lastProcessedChunkId: Long?
    ) {
        Log.d(TAG, "========================================")
        Log.d(TAG, "📤 청크 재전송 시작")
        Log.d(TAG, "총 ${chunks.size}개 청크 재전송 예정")
        Log.d(TAG, "========================================")

        var successCount = 0
        var failCount = 0

        chunks.forEach { chunkInfo ->
            try {
                // 파일에서 오디오 데이터 읽기
                val audioData = chunkInfo.file.readBytes()

                // 메타데이터 파일도 읽기 (있으면)
                val metaFile = File(packetDir, "chunk_${chunkInfo.chunkId}.json")
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
                    Log.d(TAG, "✅ 재전송 성공 - ID: ${chunkInfo.chunkId}, 크기: ${audioData.size} bytes (${successCount}/${chunks.size})")
                } else {
                    failCount++
                    Log.w(TAG, "⚠️ 재전송 실패 (큐 가득 참) - ID: ${chunkInfo.chunkId}")
                    // 큐가 가득 찬 경우 잠시 대기
                    delay(100)
                }

                // 전송 간 짧은 딜레이 (서버 부하 방지)
                delay(10)

            } catch (e: Exception) {
                failCount++
                Log.e(TAG, "❌ 청크 재전송 실패 - ID: ${chunkInfo.chunkId}", e)
            }
        }

        Log.d(TAG, "========================================")
        Log.d(TAG, "📤 청크 재전송 완료")
        Log.d(TAG, "성공: ${successCount}개, 실패: ${failCount}개")
        Log.d(TAG, "========================================")
    }

    /**
     * ✅ 저장된 청크 정보를 담는 데이터 클래스
     */
    private data class SavedChunkInfo(
        val chunkId: Long,
        val file: File
    )

    /**
     * 오디오 청크를 WebSocket으로 전송
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendAudioChunk(webSocket: WebSocket, audioData: ByteArray) {
        try {
            val chunkId = chunkIdCounter.getAndIncrement()

            // 메타데이터 JSON 생성
            val metaJson = JSONObject().apply {
                put("type", "AUDIO_CHUNK")
                put("chunkId", chunkId)
                put("encoding", "audio/pcm")  // PCM 16bit
                put("timestamp", getCurrentTimestamp())
            }

            val metaBytes = metaJson.toString().toByteArray(Charsets.UTF_8)
            val metaLength = metaBytes.size

            // 바이너리 메시지 조립
            // 프로토콜: [4 bytes: 메타 길이][N bytes: JSON 메타데이터][M bytes: 오디오 데이터]
            val buffer = ByteBuffer.allocate(4 + metaLength + audioData.size)
            buffer.putInt(metaLength)          // 메타데이터 길이
            buffer.put(metaBytes)              // JSON 메타데이터
            buffer.put(audioData)              // 오디오 데이터

            val totalSize = buffer.position()
            val binaryMessage = buffer.array().toByteString(0, totalSize)

            // ✅ 1. WebSocket으로 전송
            val success = webSocket.send(binaryMessage)

            if (success) {
                Log.d(TAG, "✅ 청크 전송 성공 - ID: $chunkId, 크기: ${audioData.size} bytes (총: $totalSize bytes)")
            } else {
                Log.w(TAG, "⚠️ 청크 전송 실패 (큐가 가득 찼을 수 있음) - ID: $chunkId")
            }

            // ✅ 2. 로컬 파일로 저장
            saveAudioChunkToFile(chunkId, audioData, metaJson)

        } catch (e: Exception) {
            Log.e(TAG, "❌ 청크 전송 예외: ${e.message}", e)
        }
    }

    /**
     * 오디오 청크를 로컬 파일로 저장
     */
    private fun saveAudioChunkToFile(chunkId: Long, audioData: ByteArray, metaJson: JSONObject) {
        try {
            // PCM 파일 저장
            val audioFile = File(packetDir, "chunk_${chunkId}.pcm")
            FileOutputStream(audioFile).use { fos ->
                fos.write(audioData)
            }

            Log.v(TAG, "💾 청크 파일 저장 성공 - ${audioFile.name} (${audioData.size} bytes)")

            // 메타데이터 저장
            val metaFile = File(packetDir, "chunk_${chunkId}.json")
            FileOutputStream(metaFile).use { fos ->
                fos.write(metaJson.toString(2).toByteArray())
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ 청크 파일 저장 실패 - ID: $chunkId", e)
        }
    }

    /**
     * ✅ 저장된 청크 파일 목록 확인
     */
    fun listSavedChunks(): List<File> {
        return try {
            packetDir.listFiles { file ->
                file.extension == "pcm"
            }?.sortedBy { it.name } ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "❌ 청크 파일 목록 조회 실패", e)
            emptyList()
        }
    }

    fun clearSavedChunks() {
        try {
            val deletedCount = packetDir.listFiles()?.count { it.delete() } ?: 0
            Log.d(TAG, "🗑️ 청크 파일 삭제 완료: $deletedCount 개")

            // 디렉토리도 삭제
            if (packetDir.listFiles()?.isEmpty() == true) {
                packetDir.delete()
                Log.d(TAG, "🗑️ 디렉토리 삭제 완료: ${packetDir.absolutePath}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ 청크 파일 삭제 실패", e)
        }
    }

    fun getSavedChunksInfo(): String {
        return try {
            val files = listSavedChunks()
            val totalSize = files.sumOf { it.length() }
            val count = files.size

            """
            저장된 청크 정보:
            - 총 개수: $count 개
            - 총 크기: ${totalSize / 1024} KB (${totalSize / 1024 / 1024} MB)
            - 저장 경로: ${packetDir.absolutePath}
            """.trimIndent()
        } catch (e: Exception) {
            "청크 정보 조회 실패: ${e.message}"
        }
    }

    /**
     * 녹음 중지
     */
    fun stopRecording() {
        if (!isRecording) {
            Log.d(TAG, "녹음이 이미 중지되어 있습니다")
            return
        }

        Log.d(TAG, "========================================")
        Log.d(TAG, "🛑 녹음 중지 요청")
        Log.d(TAG, "========================================")

        isRecording = false

        // 녹음 Job 취소
        recordingJob?.cancel()
        recordingJob = null

        try {
            // AudioRecord 중지 및 해제
            audioRecord?.apply {
                if (recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    stop()
                    Log.d(TAG, "AudioRecord 중지됨")
                }
                release()
                Log.d(TAG, "AudioRecord 리소스 해제됨")
            }
            audioRecord = null

            _recordingStatus.postValue("녹음 중지됨")
            Log.d(TAG, "✅ 녹음 중지 완료")

        } catch (e: Exception) {
            Log.e(TAG, "❌ 녹음 중지 중 에러 발생", e)
        }
    }

    /**
     * 현재 녹음 중인지 확인
     */
    fun isRecording(): Boolean = isRecording

    private fun handleDiarizedSegment(data: RealtimeSegmentDto?) {
        if (data == null) return

        Log.d(TAG, """
            🗣️ 실시간 음성→텍스트:
            - 시간: ${data.timestamp}
            - 사용자: ${data.userId}
            - 순서: ${data.order}
            - 내용: ${data.text}
        """.trimIndent())
    }

    private fun handleMeetingNoteCreated(message: String?) {
        Log.d(TAG, "📄 회의록 생성 완료: $message")
    }

    private fun handleError(error: ErrorResponse?) {
        if (error == null) return
        Log.e(TAG, "❌ 에러: ${error.errorMessage} (${error.errorCode})")
    }

    // ========================================
    // SSE 연결 테스트
    // ========================================

    /**
     * SSE 연결 테스트
     * @param serverUrl 서버 URL (예: "http://10.0.2.2:8080")
     * @param meetingId 회의 ID
     * @param jwtToken JWT 인증 토큰
     */
    fun testSseConnection(
        serverUrl: String,
        meetingId: Long,
        jwtToken: String
    ) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "========================================")
                Log.d(TAG, "SSE 연결 테스트 시작")
                Log.d(TAG, "========================================")
                Log.d(TAG, "서버 URL: $serverUrl")
                Log.d(TAG, "회의 ID: $meetingId")
                Log.d(TAG, "JWT 토큰 (앞 20자): ${jwtToken.take(20)}...")

                val sseUrl = "$serverUrl/api/v1/meetings/$meetingId/events/subscribe"
                Log.d(TAG, "SSE 전체 URL: $sseUrl")

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
                            Log.d(TAG, "✅ [SSE] 연결 성공!")
                        }

                        override fun onEvent(
                            eventSource: EventSource,
                            id: String?,
                            type: String?,
                            data: String
                        ) {
                            Log.d(TAG, "========================================")
                            Log.d(TAG, "📨 [SSE] 이벤트 수신")
                            Log.d(TAG, "타입: $type")
                            Log.d(TAG, "데이터: $data")
                            parseSseEvent(type, data)
                            Log.d(TAG, "========================================")
                        }

                        override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                            Log.e(TAG, "❌ [SSE] 실패: ${t?.message}", t)
                        }
                    })

            } catch (e: Exception) {
                Log.e(TAG, "❌ [SSE] 예외: ${e.message}", e)
            }
        }
    }

    /**
     * SSE 이벤트 파싱 (실제 전송되는 4가지 타입만)
     */
    private fun parseSseEvent(type: String?, data: String) {
        val sseType = type.toSseResponseType()
        when (sseType) {
            // 1. 연결 확인
            SseResponseType.CONNECTED -> {
                Log.d(TAG, "✅ SSE 연결 확인: $data")
            }

            // 2. 참여율 (주기적으로 전송)
            SseResponseType.PARTICIPATION_RATE -> {
                try {
                    val participationRates = gson.fromJson<Map<Long, Double>>(
                        data,
                        object : TypeToken<Map<Long, Double>>() {}.type
                    )
                    handleParticipationRate(participationRates)
                } catch (e: Exception) {
                    Log.e(TAG, "참여율 파싱 실패", e)
                }
            }

            // 3. 피드백
            SseResponseType.FEEDBACK -> {
                try {
                    val feedback = gson.fromJson(data, LiveFeedbackDto::class.java)
                    handleFeedback(feedback)
                } catch (e: Exception) {
                    Log.e(TAG, "피드백 파싱 실패", e)
                }
            }

            // 4. 요약
            SseResponseType.SUMMARY -> {
                try {
                    val summary = gson.fromJson(data, LiveSummaryDto::class.java)
                    handleSummary(summary)
                } catch (e: Exception) {
                    Log.e(TAG, "요약 파싱 실패", e)
                }
            }

            else -> {
                Log.w(TAG, "⚠️ 알 수 없는 SSE 타입: $type")
            }
        }
    }

    private fun handleParticipationRate(rates: Map<Long, Double>) {
        Log.d(TAG, "📊 참여율 업데이트:")
        rates.forEach { (userId, rate) ->
            Log.d(TAG, "  - 사용자 $userId: ${(rate * 100).toInt()}%")
        }
    }

    private fun handleFeedback(feedback: LiveFeedbackDto) {
        Log.d(TAG, "💬 피드백: ${feedback.comment}")
    }

    private fun handleSummary(summary: LiveSummaryDto) {
        Log.d(TAG, "📝 요약: ${summary.summary}")
    }

    // ========================================
    // 연결 해제
    // ========================================

    fun disconnectWebSocket() {
        stopRecording()
        webSocket?.close(1000, "테스트 종료")
        webSocket = null
        Log.d(TAG, "WebSocket 연결 해제 완료")
    }

    fun disconnectSse() {
        eventSource?.cancel()
        eventSource = null
        Log.d(TAG, "SSE 연결 해제 완료")
    }

    fun disconnectAll() {
        stopRecording()
        disconnectWebSocket()
        disconnectSse()
        Log.d(TAG, "모든 연결 해제 완료")
    }

    override fun onCleared() {
        super.onCleared()
        audioRecord?.release()
        disconnectAll()
    }

    // ========================================
    // 유틸리티
    // ========================================

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCurrentTimestamp(): String {
        return try {
            // Android API 26 이상
            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        } catch (e: Exception) {
            // Android API 26 미만
            System.currentTimeMillis().toString()
        }
    }

    /**
     * String을 SseResponseType으로 안전하게 변환
     */
    fun String?.toSseResponseType(): SseResponseType? {
        return try {
            SseResponseType.valueOf(this ?: "")
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    /**
     * String을 SocketResponseType으로 안전하게 변환
     */
    fun String?.toSocketResponseType(): SocketResponseType? {
        return try {
            SocketResponseType.valueOf(this ?: "")
        } catch (e: IllegalArgumentException) {
            null
        }
    }

	// 디버깅/시연을 위한 더미 데이터 주입
	fun loadDummyMeetingInProgressState() {
		val dummyStart = System.currentTimeMillis() - 15 * 60 * 1000 // 15분 전에 시작
		val dummyAgendas = listOf(
			com.imhungry.sillok.domain.model.agenda.Agenda(1L, "프로젝트 소개", true),
			com.imhungry.sillok.domain.model.agenda.Agenda(2L, "요구사항 논의", false),
			com.imhungry.sillok.domain.model.agenda.Agenda(3L, "액션 아이템 정리", false)
		)

		val dummySegments = listOf(
			SegmentUi(timestamp = "00:01:10", text = "안녕하세요, 오늘 아젠다는...", nickname = "홍길동", profileImage = ProfileUtils.getProfileDrawableForUser(1), isFromCurrentUser = false, isSameAsPrevious = false, isSameAsNext = true),
			SegmentUi(timestamp = "00:01:35", text = "첫 번째로 목표 범위를 정하면...", nickname = "홍길동", profileImage = ProfileUtils.getProfileDrawableForUser(1), isFromCurrentUser = false, isSameAsPrevious = true, isSameAsNext = false),
			SegmentUi(timestamp = "00:02:10", text = "디자인 관점에서 보면...", nickname = "김디자", profileImage = ProfileUtils.getProfileDrawableForUser(2), isFromCurrentUser = false, isSameAsPrevious = false, isSameAsNext = false),
			SegmentUi(timestamp = "00:03:45", text = "백엔드 API는...", nickname = "이개발", profileImage = ProfileUtils.getProfileDrawableForUser(3), isFromCurrentUser = true, isSameAsPrevious = false, isSameAsNext = false),
            SegmentUi(timestamp = "00:01:10", text = "안녕하세요, 오늘 아젠다는...", nickname = "홍길동", profileImage = ProfileUtils.getProfileDrawableForUser(1), isFromCurrentUser = false, isSameAsPrevious = false, isSameAsNext = true),
            SegmentUi(timestamp = "00:01:35", text = "첫 번째로 목표 범위를 정하면...", nickname = "홍길동", profileImage = ProfileUtils.getProfileDrawableForUser(1), isFromCurrentUser = false, isSameAsPrevious = true, isSameAsNext = false),
            SegmentUi(timestamp = "00:02:10", text = "디자인 관점에서 보면...", nickname = "김디자", profileImage = ProfileUtils.getProfileDrawableForUser(2), isFromCurrentUser = false, isSameAsPrevious = false, isSameAsNext = false),
            SegmentUi(timestamp = "00:03:45", text = "백엔드 API는...", nickname = "이개발", profileImage = ProfileUtils.getProfileDrawableForUser(3), isFromCurrentUser = true, isSameAsPrevious = false, isSameAsNext = false),
            SegmentUi(timestamp = "00:01:10", text = "안녕하세요, 오늘 아젠다는...", nickname = "홍길동", profileImage = ProfileUtils.getProfileDrawableForUser(1), isFromCurrentUser = false, isSameAsPrevious = false, isSameAsNext = true),
            SegmentUi(timestamp = "00:01:35", text = "첫 번째로 목표 범위를 정하면...", nickname = "홍길동", profileImage = ProfileUtils.getProfileDrawableForUser(1), isFromCurrentUser = false, isSameAsPrevious = true, isSameAsNext = false),
            SegmentUi(timestamp = "00:02:10", text = "디자인 관점에서 보면...", nickname = "김디자", profileImage = ProfileUtils.getProfileDrawableForUser(2), isFromCurrentUser = false, isSameAsPrevious = false, isSameAsNext = false),
            SegmentUi(timestamp = "00:03:45", text = "백엔드 API는...", nickname = "이개발", profileImage = ProfileUtils.getProfileDrawableForUser(3), isFromCurrentUser = true, isSameAsPrevious = false, isSameAsNext = false),
            SegmentUi(timestamp = "00:01:10", text = "안녕하세요, 오늘 아젠다는...", nickname = "홍길동", profileImage = ProfileUtils.getProfileDrawableForUser(1), isFromCurrentUser = false, isSameAsPrevious = false, isSameAsNext = true),
            SegmentUi(timestamp = "00:01:35", text = "첫 번째로 목표 범위를 정하면...", nickname = "홍길동", profileImage = ProfileUtils.getProfileDrawableForUser(1), isFromCurrentUser = false, isSameAsPrevious = true, isSameAsNext = false),
            SegmentUi(timestamp = "00:02:10", text = "디자인 관점에서 보면...", nickname = "김디자", profileImage = ProfileUtils.getProfileDrawableForUser(2), isFromCurrentUser = false, isSameAsPrevious = false, isSameAsNext = false),
            SegmentUi(timestamp = "00:03:45", text = "백엔드 API는...", nickname = "이개발", profileImage = ProfileUtils.getProfileDrawableForUser(3), isFromCurrentUser = true, isSameAsPrevious = false, isSameAsNext = false),
            SegmentUi(timestamp = "00:01:10", text = "안녕하세요, 오늘 아젠다는...", nickname = "홍길동", profileImage = ProfileUtils.getProfileDrawableForUser(1), isFromCurrentUser = false, isSameAsPrevious = false, isSameAsNext = true),
            SegmentUi(timestamp = "00:01:35", text = "첫 번째로 목표 범위를 정하면...", nickname = "홍길동", profileImage = ProfileUtils.getProfileDrawableForUser(1), isFromCurrentUser = false, isSameAsPrevious = true, isSameAsNext = false),
            SegmentUi(timestamp = "00:02:10", text = "디자인 관점에서 보면...", nickname = "김디자", profileImage = ProfileUtils.getProfileDrawableForUser(2), isFromCurrentUser = false, isSameAsPrevious = false, isSameAsNext = false),
            SegmentUi(timestamp = "00:03:45", text = "백엔드 API는...", nickname = "이개발", profileImage = ProfileUtils.getProfileDrawableForUser(3), isFromCurrentUser = true, isSameAsPrevious = false, isSameAsNext = false),
            SegmentUi(timestamp = "00:01:10", text = "안녕하세요, 오늘 아젠다는...", nickname = "홍길동", profileImage = ProfileUtils.getProfileDrawableForUser(1), isFromCurrentUser = false, isSameAsPrevious = false, isSameAsNext = true),
            SegmentUi(timestamp = "00:01:35", text = "첫 번째로 목표 범위를 정하면...", nickname = "홍길동", profileImage = ProfileUtils.getProfileDrawableForUser(1), isFromCurrentUser = false, isSameAsPrevious = true, isSameAsNext = false),
            SegmentUi(timestamp = "00:02:10", text = "디자인 관점에서 보면...", nickname = "김디자", profileImage = ProfileUtils.getProfileDrawableForUser(2), isFromCurrentUser = false, isSameAsPrevious = false, isSameAsNext = false),
            SegmentUi(timestamp = "00:03:45", text = "백엔드 API는...", nickname = "이개발", profileImage = ProfileUtils.getProfileDrawableForUser(3), isFromCurrentUser = true, isSameAsPrevious = false, isSameAsNext = false),
            SegmentUi(timestamp = "00:01:10", text = "안녕하세요, 오늘 아젠다는...", nickname = "홍길동", profileImage = ProfileUtils.getProfileDrawableForUser(1), isFromCurrentUser = false, isSameAsPrevious = false, isSameAsNext = true),
            SegmentUi(timestamp = "00:01:35", text = "첫 번째로 목표 범위를 정하면...", nickname = "홍길동", profileImage = ProfileUtils.getProfileDrawableForUser(1), isFromCurrentUser = false, isSameAsPrevious = true, isSameAsNext = false),
            SegmentUi(timestamp = "00:02:10", text = "디자인 관점에서 보면...", nickname = "김디자", profileImage = ProfileUtils.getProfileDrawableForUser(2), isFromCurrentUser = false, isSameAsPrevious = false, isSameAsNext = false),
            SegmentUi(timestamp = "00:03:45", text = "백엔드 API는...", nickname = "이개발", profileImage = ProfileUtils.getProfileDrawableForUser(3), isFromCurrentUser = true, isSameAsPrevious = false, isSameAsNext = false)
		)

		val dummySummaries = listOf(
			SummaryUi(content = "회의 목적과 범위를 합의함", timestamp = "00:10:00"),
			SummaryUi(content = "핵심 액션 아이템 3개 도출", timestamp = "00:12:30"),
            SummaryUi(content = "회의 목적과 범위를 합의함", timestamp = "00:10:00"),
            SummaryUi(content = "핵심 액션 아이템 3개 도출", timestamp = "00:12:30"),
            SummaryUi(content = "회의 목적과 범위를 합의함", timestamp = "00:10:00"),
            SummaryUi(content = "핵심 액션 아이템 3개 도출", timestamp = "00:12:30"),
            SummaryUi(content = "회의 목적과 범위를 합의함", timestamp = "00:10:00"),
            SummaryUi(content = "핵심 액션 아이템 3개 도출", timestamp = "00:12:30"),
            SummaryUi(content = "회의 목적과 범위를 합의함", timestamp = "00:10:00"),
            SummaryUi(content = "핵심 액션 아이템 3개 도출", timestamp = "00:12:30"),
            SummaryUi(content = "회의 목적과 범위를 합의함", timestamp = "00:10:00"),
            SummaryUi(content = "핵심 액션 아이템 3개 도출", timestamp = "00:12:30"),
            SummaryUi(content = "회의 목적과 범위를 합의함", timestamp = "00:10:00"),
            SummaryUi(content = "핵심 액션 아이템 3개 도출", timestamp = "00:12:30"),
            SummaryUi(content = "회의 목적과 범위를 합의함", timestamp = "00:10:00"),
            SummaryUi(content = "핵심 액션 아이템 3개 도출", timestamp = "00:12:30"),
            SummaryUi(content = "회의 목적과 범위를 합의함", timestamp = "00:10:00"),
            SummaryUi(content = "핵심 액션 아이템 3개 도출", timestamp = "00:12:30"),
            SummaryUi(content = "회의 목적과 범위를 합의함", timestamp = "00:10:00"),
            SummaryUi(content = "핵심 액션 아이템 3개 도출", timestamp = "00:12:30")
		)

		val dummyParticipation = listOf(
			UserParticipationRate(userId = 1L, nickname = "홍길동", rate = 0.45),
			UserParticipationRate(userId = 2L, nickname = "김디자", rate = 0.35),
			UserParticipationRate(userId = 3L, nickname = "이개발", rate = 0.20)
		)

		val dummyFeedbacks = listOf(
			FeedbackUi(comment = "속도 좋습니다!", timestamp = "00:14:10", isRead = true),
			FeedbackUi(comment = "요구사항 정리 항목 추가 제안", timestamp = "00:14:50", isRead = true),
            FeedbackUi(comment = "속도 좋습니다!", timestamp = "00:14:10", isRead = true),
            FeedbackUi(comment = "요구사항 정리 항목 추가 제안", timestamp = "00:14:50", isRead = true),
            FeedbackUi(comment = "속도 좋습니다!", timestamp = "00:14:10", isRead = true),
            FeedbackUi(comment = "요구사항 정리 항목 추가 제안", timestamp = "00:14:50", isRead = true),
            FeedbackUi(comment = "속도 좋습니다!", timestamp = "00:14:10", isRead = true),
            FeedbackUi(comment = "요구사항 정리 항목 추가 제안", timestamp = "00:14:50", isRead = true),
            FeedbackUi(comment = "속도 좋습니다!", timestamp = "00:14:10", isRead = true),
            FeedbackUi(comment = "요구사항 정리 항목 추가 제안", timestamp = "00:14:50", isRead = true),
            FeedbackUi(comment = "속도 좋습니다!", timestamp = "00:14:10", isRead = true),
            FeedbackUi(comment = "요구사항 정리 항목 추가 제안", timestamp = "00:14:50", isRead = true),
            FeedbackUi(comment = "속도 좋습니다!", timestamp = "00:14:10", isRead = true),
            FeedbackUi(comment = "요구사항 정리 항목 추가 제안", timestamp = "00:14:50", isRead = true),
            FeedbackUi(comment = "속도 좋습니다!", timestamp = "00:14:10", isRead = true),
            FeedbackUi(comment = "요구사항 정리 항목 추가 제안", timestamp = "00:14:50", isRead = true),
            FeedbackUi(comment = "속도 좋습니다!", timestamp = "00:14:10", isRead = true),
            FeedbackUi(comment = "요구사항 정리 항목 추가 제안", timestamp = "00:14:50", isRead = true),
            FeedbackUi(comment = "속도 좋습니다!", timestamp = "00:14:10", isRead = true),
            FeedbackUi(comment = "요구사항 정리 항목 추가 제안", timestamp = "00:14:50", isRead = true),
            FeedbackUi(comment = "속도 좋습니다!", timestamp = "00:14:10", isRead = true),
            FeedbackUi(comment = "요구사항 정리 항목 추가 제안", timestamp = "00:14:50", isRead = true),
            FeedbackUi(comment = "속도 좋습니다!", timestamp = "00:14:10", isRead = true),
            FeedbackUi(comment = "요구사항 정리 항목 추가 제안", timestamp = "00:14:50", isRead = true),
            FeedbackUi(comment = "속도 좋습니다!", timestamp = "00:14:10", isRead = true),
            FeedbackUi(comment = "요구사항 정리 항목 추가 제안", timestamp = "00:14:50", isRead = true)
		)

		_state.update {
			it.copy(
				meetingId = 5555L,
				agendas = dummyAgendas,
				segments = dummySegments,
				summaries = dummySummaries,
				participationRates = dummyParticipation,
				feedbacks = dummyFeedbacks,
				startTime = dummyStart,
				isLoading = false,
				error = null
			)
		}
	}
}