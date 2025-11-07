package com.imhungry.sillok.presentation.viewmodel.meeting

import android.app.Application
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.imhungry.sillok.BuildConfig
import com.imhungry.sillok.data.local.FeedbackReadStore
import com.imhungry.sillok.data.local.TokenStore
import com.imhungry.sillok.data.local.UserStore
import com.imhungry.sillok.data.model.realtime.ErrorResponse
import com.imhungry.sillok.data.model.realtime.LiveFeedbackDto
import com.imhungry.sillok.data.model.realtime.LiveSummaryDto
import com.imhungry.sillok.data.model.realtime.RealtimeSegmentDto
import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.model.meeting.MeetingStatus
import com.imhungry.sillok.domain.model.participation.UserParticipationRate
import com.imhungry.sillok.domain.usecase.agenda.ChangeAgendaStatusUseCase
import com.imhungry.sillok.domain.usecase.agenda.GetAgendasUseCase
import com.imhungry.sillok.domain.usecase.feedback.GetFeedbacksUseCase
import com.imhungry.sillok.domain.usecase.meeting.GetMeetingDetailUseCase
import com.imhungry.sillok.domain.usecase.meeting.UpdateMeetingStatusUseCase
import com.imhungry.sillok.domain.usecase.participation.GetParticipationRateHistoryUseCase
import com.imhungry.sillok.domain.usecase.segment.GetSegmentsUseCase
import com.imhungry.sillok.domain.usecase.summary.GetSummariesUseCase
import com.imhungry.sillok.domain.usecase.user.GetUserInfoUseCase
import com.imhungry.sillok.presentation.service.MeetingInProgressService
import com.imhungry.sillok.presentation.state.meeting.FeedbackUi
import com.imhungry.sillok.presentation.state.meeting.MeetingInProgressEvent
import com.imhungry.sillok.presentation.state.meeting.MeetingInProgressState
import com.imhungry.sillok.presentation.state.meeting.SegmentUi
import com.imhungry.sillok.presentation.state.meeting.SummaryUi
import com.imhungry.sillok.presentation.util.DateTimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class MeetingInProgressViewModel @Inject constructor(
    private val getAgendasUseCase: GetAgendasUseCase,
    private val changeAgendaStatusUseCase: ChangeAgendaStatusUseCase,
    private val getSegmentsUseCase: GetSegmentsUseCase,
    private val getSummariesUseCase: GetSummariesUseCase,
    private val getParticipationRateHistoryUseCase: GetParticipationRateHistoryUseCase,
    private val getFeedbacksUseCase: GetFeedbacksUseCase,
    private val getMeetingDetailUseCase: GetMeetingDetailUseCase,
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val updateMeetingStatusUseCase: UpdateMeetingStatusUseCase,
    private val userStore: UserStore,
    private val tokenStore: TokenStore,
    private val feedbackReadStore: FeedbackReadStore,
    private val app: Application,
) : AndroidViewModel(app) {
    companion object {
        private const val TAG = "MeetingInProgressViewModel"
    }

    private val _state = MutableStateFlow(MeetingInProgressState())
    val state: StateFlow<MeetingInProgressState> = _state.asStateFlow()

    private val _micEnabled = MutableStateFlow(true)
    val micEnabled: StateFlow<Boolean> = _micEnabled.asStateFlow()

    private val _events = MutableSharedFlow<MeetingInProgressEvent>()
    val events: SharedFlow<MeetingInProgressEvent> = _events.asSharedFlow()

    // 스케줄링된 피드백 (휴식 시간, 종료 시간 알림용)
    private val _scheduledFeedback = MutableStateFlow<FeedbackUi?>(null)
    val scheduledFeedback: StateFlow<FeedbackUi?> = _scheduledFeedback.asStateFlow()

    // 쉬는 시간 범위 리스트 (시작 시간, 종료 시간을 경과 시간 문자열로 저장)
    private val _restBreakPeriods = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val restBreakPeriods: StateFlow<List<Pair<String, String>>> = _restBreakPeriods.asStateFlow()

    // 사용자 정보 캐시 (userId -> nickname)
    private val userNicknameCache = mutableMapOf<Long, String>()
    
    // 스케줄링 Job 추적 (중복 실행 방지)
    private var restBreakSchedulingJob: Job? = null
    private var meetingEndSchedulingJob: Job? = null

    @RequiresApi(Build.VERSION_CODES.O)
    fun initialize(meetingId: Long) {
        _state.update { it.copy(meetingId = meetingId) }
        viewModelScope.launch {
            // refreshAll() 완료 후 Service 시작
            refreshAll()

            // TokenStore에서 토큰 가져오기
            val jwtToken = tokenStore.accessToken.first()
            if (jwtToken == null) {
                return@launch
            }
            
            // Service 시작
            startService(
                serverUrl = BuildConfig.BASE_URL,
                meetingId = meetingId,
                jwtToken = jwtToken
            )
            
            // Service 이벤트 구독
            observeServiceEvents()
        }
    }
    
    /**
     * Service 시작
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun startService(serverUrl: String, meetingId: Long, jwtToken: String) {
        val intent = Intent(app, MeetingInProgressService::class.java).apply {
            action = MeetingInProgressService.ACTION_START
            putExtra(MeetingInProgressService.EXTRA_SERVER_URL, serverUrl)
            putExtra(MeetingInProgressService.EXTRA_MEETING_ID, meetingId)
            putExtra(MeetingInProgressService.EXTRA_JWT_TOKEN, jwtToken)
        }
        ContextCompat.startForegroundService(app, intent)
        Log.d(TAG, "Service 시작 요청: meetingId=$meetingId")
    }
    
    /**
     * Service 이벤트 구독
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun observeServiceEvents() {
        viewModelScope.launch {
            MeetingInProgressService.serviceEvents.collectLatest { event ->
                when (event) {
                    is MeetingInProgressService.ServiceEvent.ConnectionEstablished -> {
                        _state.update { it.copy(isLoading = false) }
                        handleConnectionEstablishedFromService(event.lastProcessedChunkId)
                    }
                    is MeetingInProgressService.ServiceEvent.DiarizedSegment -> {
                        handleDiarizedSegment(event.segment)
                    }
                    is MeetingInProgressService.ServiceEvent.MeetingCompleted -> {
                        handleMeetingCompleted()
                    }
                    is MeetingInProgressService.ServiceEvent.MeetingNoteCreated -> {
                        handleMeetingNoteCreated(event.message)
                    }
                    is MeetingInProgressService.ServiceEvent.ParticipationRate -> {
                        handleParticipationRate(event.rates)
                    }
                    is MeetingInProgressService.ServiceEvent.Feedback -> {
                        handleFeedback(event.feedback)
                    }
                    is MeetingInProgressService.ServiceEvent.Summary -> {
                        handleSummary(event.summary)
                    }
                    is MeetingInProgressService.ServiceEvent.Error -> {
                        handleError(event.error)
                    }
                }
            }
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleConnectionEstablishedFromService(lastProcessedChunkId: Long?) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val meetingId = state.value.meetingId
                val currentTimeMillis = System.currentTimeMillis()
                
                // Firebase에 startMillis 업데이트
                try {
                    FirebaseFirestore.getInstance()
                        .collection("meetings")
                        .document(meetingId.toString())
                        .update("startMillis", currentTimeMillis)
                        .await()
                    
                    // state 업데이트
                    withContext(Dispatchers.Main) {
                        _state.update { it.copy(startTime = currentTimeMillis) }
                        
                        // 휴식 시간 피드백 스케줄링
                        val currentState = state.value
                        if (currentState.targetTime > 0 && currentState.restInterval > 0 && currentState.restDuration > 0) {
                            scheduleRestBreakFeedbacks(
                                currentTimeMillis,
                                currentState.targetTime,
                                currentState.restInterval,
                                currentState.restDuration
                            )
                        }
                        
                        // 회의 종료 10분 전 피드백 스케줄링
                        if (currentState.targetTime > 0) {
                            scheduleMeetingEndFeedback(currentTimeMillis, currentState.targetTime)
                        }
                    }
                    Log.d(TAG, "Firebase startMillis 업데이트 완료: $currentTimeMillis")
                } catch (e: Exception) {
                    Log.e(TAG, "Firebase startMillis 업데이트 실패: ${e.message}", e)
                }
            } catch (e: Exception) {
                Log.e(TAG, "연결 확립 처리 중 오류", e)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun refreshAll() {
        _state.update { it.copy(isLoading = true) }
        val meetingId = state.value.meetingId

        coroutineScope {
            // 1. Meeting Detail 가져오기 (participants 정보 포함)
            when (val meetingDetailResult = getMeetingDetailUseCase(meetingId)) {
                is ApiResult.Success -> {
                    val meeting = meetingDetailResult.data
                    // targetTime, restInterval, restDuration 저장
                    _state.update {
                        it.copy(
                            targetTime = meeting.targetTime,
                            restInterval = meeting.restInterval,
                            restDuration = meeting.restDuration
                        )
                    }

                    // participants의 email로 사용자 정보 미리 로드 (병렬 처리)
                    meeting.participants.forEach { participant ->
                        launch(Dispatchers.IO) {
                            try {
                                when (val userResult = getUserInfoUseCase(participant.email)) {
                                    is ApiResult.Success -> {
                                        userNicknameCache[participant.userId] =
                                            userResult.data.nickname
                                        Log.d(
                                            TAG,
                                            "사용자 정보 캐시 저장: userId=${participant.userId}, nickname=${userResult.data.nickname}"
                                        )
                                    }

                                    is ApiResult.Failure -> {
                                        Log.w(
                                            TAG,
                                            "사용자 정보 조회 실패 (userId: ${participant.userId}, email: ${participant.email}): ${userResult.message}"
                                        )
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e(
                                    TAG,
                                    "사용자 정보 조회 예외 (userId: ${participant.userId}, email: ${participant.email}): ${e.message}",
                                    e
                                )
                            }
                        }
                    }
                }

                is ApiResult.Failure -> {
                    Log.e(TAG, "Meeting Detail 로드 실패: ${meetingDetailResult.message}")
                }
            }

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
                    
                    // 휴식 시간 피드백 스케줄링
                    val currentState = state.value
                    if (currentState.targetTime > 0 && currentState.restInterval > 0 && currentState.restDuration > 0) {
                        scheduleRestBreakFeedbacks(
                            startMillis,
                            currentState.targetTime,
                            currentState.restInterval,
                            currentState.restDuration
                        )
                    }
                    
                    // 회의 종료 10분 전 피드백 스케줄링
                    if (currentState.targetTime > 0) {
                        scheduleMeetingEndFeedback(startMillis, currentState.targetTime)
                    }
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
                                profileImage = "",
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
                        
                        // 저장된 마지막 읽은 피드백 인덱스 조회
                        val lastReadIndex = feedbackReadStore.getLastReadFeedbackIndex(meetingId)
                        
                        // 저장된 인덱스까지는 읽음 처리
                        val finalUi = if (lastReadIndex != null && lastReadIndex >= 0) {
                            ui.mapIndexed { index, feedback ->
                                if (index <= lastReadIndex) {
                                    feedback.copy(isRead = true)
                                } else {
                                    feedback
                                }
                            }
                        } else {
                            ui
                        }
                        
                        _state.update { it.copy(feedbacks = finalUi) }
                    }
					is ApiResult.Failure -> Log.e(TAG, "피드백 로드 실패: ${result.message}")
                }
            }
        }
    }

    fun markFeedbackReadAt(index: Int) {
        // 특정 인덱스의 피드백을 읽음 처리합니다.
        val current = state.value.feedbacks
        if (index !in current.indices) return
        val updated = current.toMutableList()
        updated[index] = updated[index].copy(isRead = true)
        _state.update { it.copy(feedbacks = updated) }
        
        // 로컬 저장소에 읽은 피드백 인덱스 저장
        viewModelScope.launch {
            val meetingId = state.value.meetingId
            feedbackReadStore.markFeedbackAsRead(meetingId, index)
            Log.d(TAG, "읽은 피드백 저장: meetingId=$meetingId, index=$index")
        }
    }

    fun markAllFeedbacksAsRead() {
        // 모든 피드백을 읽음 처리합니다.
        val current = state.value.feedbacks
        val updated = current.map { it.copy(isRead = true) }
        _state.update { it.copy(feedbacks = updated) }
        
        // 로컬 저장소에 모든 읽은 피드백 인덱스 저장
        viewModelScope.launch {
            val meetingId = state.value.meetingId
            feedbackReadStore.markAllFeedbacksAsRead(meetingId, current.size)
            Log.d(TAG, "모든 피드백 읽음 처리 저장: meetingId=$meetingId, count=${current.size}")
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    /**
     * 회의를 완료 상태로 변경
     */
    fun completeMeeting() {
        viewModelScope.launch {
            val meetingId = state.value.meetingId
            when (val result = updateMeetingStatusUseCase(meetingId, MeetingStatus.COMPLETED.name)) {
                is ApiResult.Success -> {
                    Log.d(TAG, "회의 완료 처리 성공: meetingId=$meetingId")

                    // Firebase에 endMillis 업데이트
                    try {
                        val endMillis = System.currentTimeMillis()
                        FirebaseFirestore.getInstance()
                            .collection("meetings")
                            .document(meetingId.toString())
                            .update("endMillis", endMillis)
                            .await()
                        Log.d(TAG, "Firebase endMillis 업데이트 완료: $endMillis")
                    } catch (e: Exception) {
                        Log.e(TAG, "Firebase endMillis 업데이트 실패: ${e.message}", e)
                    }
                }
                is ApiResult.Failure -> {
                    Log.e(TAG, "회의 완료 처리 실패: ${result.message}")
                    _state.update { it.copy(error = result.message) }
                }
            }
        }
    }

    /**
     * 마이크 토글 (Service에 전달)
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun toggleMic() {
        val intent = Intent(app, MeetingInProgressService::class.java).apply {
            action = MeetingInProgressService.ACTION_TOGGLE_MIC
        }
        app.startService(intent)
        val newState = !_micEnabled.value
        _micEnabled.value = newState
        Log.d(TAG, "마이크 토글: ${if (newState) "켜짐" else "꺼짐"}")
    }

    fun changeAgendaStatus(meetingId: Long, agendaId: Long, isCompleted: Boolean) {
        val previous = state.value.agendas
        val updated = previous.map { agenda ->
            if (agenda.agendaId == agendaId) agenda.copy(isCompleted = isCompleted) else agenda
        }
        _state.update { it.copy(agendas = updated) }


        viewModelScope.launch {
            when (val result = changeAgendaStatusUseCase(meetingId, agendaId, isCompleted)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(agendas = updated) }
                }
                is ApiResult.Failure -> {
                    _state.update { it.copy(agendas = previous, error = result.message) }
                }
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleDiarizedSegment(data: RealtimeSegmentDto?) {
        if (data == null) return

        Log.d(TAG, """
            실시간 음성→텍스트:
            - 시간: ${data.timestamp}
            - 사용자: ${data.userId}
            - 순서: ${data.order}
            - 내용: ${data.text}
        """.trimIndent())
        
        // state에 세그먼트 추가
        viewModelScope.launch {
            val currentState = state.value
            val startMillis = currentState.startTime
            val currentUserId = userStore.user.first()?.id
            
            if (startMillis > 0) {
                val currentSegments = currentState.segments.toMutableList()
                val lastSegment = currentSegments.lastOrNull()
                
                // 캐시에서 nickname 가져오기 (없으면 기본값 사용)
                val nickname = userNicknameCache[data.userId] ?: "사용자 ${data.userId}"
                val isSameAsPrevious = lastSegment != null && lastSegment.nickname == nickname
                
                val segmentUi = SegmentUi(
                    timestamp = DateTimeUtils.getElapsedString(startMillis, data.timestamp),
                    text = data.text,
                    nickname = nickname,
                    profileImage = "",
                    isFromCurrentUser = currentUserId != null && data.userId == currentUserId,
                    isSameAsPrevious = isSameAsPrevious,
                    isSameAsNext = false // 다음 세그먼트는 아직 없으므로 false
                )
                
                // 이전 세그먼트의 isSameAsNext 업데이트
                if (lastSegment != null && isSameAsPrevious) {
                    val lastIndex = currentSegments.lastIndex
                    currentSegments[lastIndex] = lastSegment.copy(isSameAsNext = true)
                }
                
                currentSegments.add(segmentUi)
                
                _state.update { it.copy(segments = currentSegments) }
            }
        }
    }

    /**
     * MEETING_NOTE_CREATED 수신 시 처리
     * Firebase에 회의록 생성 완료 상태 업데이트 및 연결 해제
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleMeetingNoteCreated(message: String?) {
        Log.d(TAG, "========================================")
        Log.d(TAG, "회의록 생성 완료: $message")
        Log.d(TAG, "========================================")
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val meetingId = state.value.meetingId
                
                // Firebase에 회의록 생성 완료 상태 업데이트 (generatingMeetingNoteId 제거)
                FirebaseFirestore.getInstance()
                    .collection("meetings")
                    .document(meetingId.toString())
                    .update("generatingMeetingNoteId", null)
                    .await()
                
                Log.d(TAG, "Firebase generatingMeetingNoteId 제거 완료: meetingId=$meetingId")
                
                // 웹소켓과 SSE 연결 해제
                disconnectAll()
                Log.d(TAG, "회의록 생성 완료 후 연결 해제 완료")
            } catch (e: Exception) {
                Log.e(TAG, "Firebase generatingMeetingNoteId 제거 실패: ${e.message}", e)
            }
        }
    }

    /**
     * MEETING_COMPLETED 수신 시 처리
     * Firebase에 회의록 생성 중인 회의 id 저장, 음성 녹음 및 SSE 연결 해제, 홈으로 이동 이벤트 발생
     */
    private fun handleMeetingCompleted() {
        Log.d(TAG, "========================================")
        Log.d(TAG, "회의 완료 (회의록 생성 시작)")
        Log.d(TAG, "========================================")
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val meetingId = state.value.meetingId
                
                // Firebase에 회의록 생성 중인 회의 id 저장
                FirebaseFirestore.getInstance()
                    .collection("meetings")
                    .document(meetingId.toString())
                    .update("generatingMeetingNoteId", meetingId)
                    .await()
                
                Log.d(TAG, "Firebase generatingMeetingNoteId 업데이트 완료: meetingId=$meetingId")
                
                // Service에서 녹음과 SSE 연결은 이미 해제되었으므로, Service는 회의록 생성 완료를 기다리도록 유지
                // (MEETING_NOTE_CREATED를 받으면 disconnectAll() 호출)
                
                // 홈으로 이동 이벤트 발생
                _events.emit(MeetingInProgressEvent.NavigateToHome)
            } catch (e: Exception) {
                Log.e(TAG, "Firebase generatingMeetingNoteId 업데이트 실패: ${e.message}", e)
            }
        }
    }

    private fun handleError(error: ErrorResponse?) {
        if (error == null) return
        Log.e(TAG, "❌ 에러: ${error.errorMessage} (${error.errorCode})")
    }


    private fun handleParticipationRate(rates: Map<Long, Double>) {
        Log.d(TAG, "참여율 업데이트:")
        rates.forEach { (userId, rate) ->
            Log.d(TAG, "  - 사용자 $userId: ${(rate * 100).toInt()}%")
        }
        
        // state에 참여율 업데이트
        viewModelScope.launch {
            val currentState = state.value
            val existingRatesMap = currentState.participationRates.associateBy { it.userId }
            
            // 새로운 참여율로 업데이트 (nickname 조회)
            val updatedRates = rates.map { (userId, rate) ->
                existingRatesMap[userId]?.let { existing ->
                    existing.copy(rate = rate)
                } ?: UserParticipationRate(
                    userId = userId,
                    nickname = userNicknameCache[userId] ?: "사용자 $userId",
                    rate = rate
                )
            }
            
            // 기존에 있던 사용자 중 업데이트되지 않은 사용자는 유지
            val userIdsInUpdate = rates.keys
            val remainingRates = existingRatesMap.values.filter { it.userId !in userIdsInUpdate }
            
            val allRates = (updatedRates + remainingRates).sortedByDescending { it.rate }
            
            _state.update { it.copy(participationRates = allRates) }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleFeedback(feedback: LiveFeedbackDto) {
        Log.d(TAG, "피드백: ${feedback.comment}")
        
        // state에 피드백 추가
        viewModelScope.launch {
            val currentState = state.value
            val startMillis = currentState.startTime
            val currentFeedbacks = currentState.feedbacks.toMutableList()
            
            if (startMillis > 0) {
                val currentIsoTimestamp = getCurrentTimestamp()
                
                val feedbackUi = FeedbackUi(
                    comment = feedback.comment,
                    timestamp = DateTimeUtils.getElapsedString(startMillis, currentIsoTimestamp),
                    isRead = false
                )
                
                currentFeedbacks.add(feedbackUi)
                
                _state.update { it.copy(feedbacks = currentFeedbacks) }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleSummary(summary: LiveSummaryDto) {
        Log.d(TAG, "요약: ${summary.summary}")
        
        // state에 요약 추가
        viewModelScope.launch {
            val currentState = state.value
            val startMillis = currentState.startTime
            val currentSummaries = currentState.summaries.toMutableList()
            
            if (startMillis > 0) {
                val currentIsoTimestamp = getCurrentTimestamp()
                
                val summaryUi = SummaryUi(
                    content = summary.summary,
                    timestamp = DateTimeUtils.getElapsedString(startMillis, currentIsoTimestamp)
                )
                
                currentSummaries.add(summaryUi)
                
                _state.update { it.copy(summaries = currentSummaries) }
            }
        }
    }

    // ========================================
    // 연결 해제
    // ========================================

    @RequiresApi(Build.VERSION_CODES.O)
    fun disconnectAll() {
        // Service 중지
        stopService()
        Log.d(TAG, "모든 연결 해제 완료")
    }
    
    /**
     * Service 중지
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun stopService() {
        val intent = Intent(app, MeetingInProgressService::class.java).apply {
            action = MeetingInProgressService.ACTION_STOP
        }
        app.startService(intent)
        Log.d(TAG, "Service 중지 요청")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCleared() {
        super.onCleared()
        disconnectAll()
    }

    // ========================================
    // 유틸리티
    // ========================================

    /**
     * 휴식 시간 피드백 생성 및 스케줄링
     * startMillis 기준으로 restInterval마다 휴식 시간이 있고, 각 휴식 시간 시작 1분 전에 피드백 알림 표시
     * 쉬는 시간 범위도 계산하여 StateFlow에 저장
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun scheduleRestBreakFeedbacks(startMillis: Long, targetTime: Int, restInterval: Int, restDuration: Int) {
        if (restInterval <= 0 || restDuration <= 0) return
        
        // 기존 스케줄링 Job 취소 (중복 방지)
        restBreakSchedulingJob?.cancel()
        
        // 쉬는 시간 범위 계산 및 저장
        viewModelScope.launch {
            val targetMillis = targetTime * 60 * 1000L
            var restStartMillis = startMillis + restInterval * 60 * 1000L // 첫 번째 휴식 시작 시간
            val restBreakPeriodsList = mutableListOf<Pair<String, String>>()
            
            while (restStartMillis < startMillis + targetMillis) {
                val restEndMillis = restStartMillis + restDuration * 60 * 1000L // 휴식 종료 시간
                
                // 쉬는 시간 범위를 경과 시간 문자열로 변환
                val restStartElapsed = DateTimeUtils.getElapsedStringFromMillis(startMillis, restStartMillis)
                val restEndElapsed = DateTimeUtils.getElapsedStringFromMillis(startMillis, restEndMillis)
                
                restBreakPeriodsList.add(Pair(restStartElapsed, restEndElapsed))
                
                // 다음 휴식 시간으로 이동
                restStartMillis += restInterval * 60 * 1000L
            }
            
            _restBreakPeriods.value = restBreakPeriodsList
        }
        
        // 쉬는 시간 1분 전 알림 스케줄링
        restBreakSchedulingJob = viewModelScope.launch(Dispatchers.IO) {
            val targetMillis = targetTime * 60 * 1000L
            var restStartMillis = startMillis + restInterval * 60 * 1000L // 첫 번째 휴식 시작 시간
            
            while (restStartMillis < startMillis + targetMillis) {
                val restEndMillis = restStartMillis + restDuration * 60 * 1000L // 휴식 종료 시간
                val feedbackTimeMillis = restStartMillis - 60 * 1000L // 휴식 시작 1분 전
                
                // 현재 시간 이후의 휴식 시간만 처리
                val delayMillis = feedbackTimeMillis - System.currentTimeMillis()
                if (delayMillis > 0) {
                    delay(delayMillis)
                    
                    // 휴식 시간 포맷팅 (HH:MM 형식)
                    val restStartTime = DateTimeUtils.millisToHourMinute(restStartMillis)
                    val restEndTime = DateTimeUtils.millisToHourMinute(restEndMillis)
                    
                    val feedbackMessage = "잠시 후 휴식 시간입니다.\n쉬는 시간: $restStartTime ~ $restEndTime"
                    val feedbackTimestamp = DateTimeUtils.getElapsedStringFromMillis(startMillis, feedbackTimeMillis)
                    
                    val feedbackUi = FeedbackUi(
                        comment = feedbackMessage,
                        timestamp = feedbackTimestamp,
                        isRead = false
                    )
                    
                    // 스케줄링된 피드백 StateFlow에 업데이트
                    _scheduledFeedback.value = feedbackUi
                    Log.d(TAG, "휴식 시간 피드백 알림: $feedbackMessage (시간: $restStartTime ~ $restEndTime)")
                }
                
                // 다음 휴식 시간으로 이동
                restStartMillis += restInterval * 60 * 1000L
            }
        }
    }
    
    /**
     * 회의 종료 10분 전 피드백 생성 및 스케줄링
     * startMillis 기준으로 targetTime이 끝나기 10분 전에 피드백 알림 표시
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun scheduleMeetingEndFeedback(startMillis: Long, targetTime: Int) {
        if (targetTime <= 0) return
        
        // 기존 스케줄링 Job 취소 (중복 방지)
        meetingEndSchedulingJob?.cancel()
        
        meetingEndSchedulingJob = viewModelScope.launch(Dispatchers.IO) {
            // 회의 종료 시간 계산
            val endMillis = startMillis + targetTime * 60 * 1000L
            val feedbackTimeMillis = endMillis - 10 * 60 * 1000L // 종료 10분 전
            
            // 현재 시간 이후의 피드백만 처리
            val delayMillis = feedbackTimeMillis - System.currentTimeMillis()
            if (delayMillis > 0) {
                delay(delayMillis)
                
                // 종료 예정 시각 포맷팅 (HH:MM 형식)
                val endTime = DateTimeUtils.millisToHourMinute(endMillis)
                
                val feedbackMessage = "회의 종료까지 10분 남았습니다.\n예정 종료 시각: $endTime"
                val feedbackTimestamp = DateTimeUtils.getElapsedStringFromMillis(startMillis, feedbackTimeMillis)
                
                val feedbackUi = FeedbackUi(
                    comment = feedbackMessage,
                    timestamp = feedbackTimestamp,
                    isRead = false
                )
                
                // 스케줄링된 피드백 StateFlow에 업데이트
                _scheduledFeedback.value = feedbackUi
                Log.d(TAG, "회의 종료 피드백 알림: $feedbackMessage (종료 시각: $endTime)")
            }
        }
    }
    
    /**
     * 스케줄링된 피드백 알림 해제
     */
    fun dismissScheduledFeedback() {
        _scheduledFeedback.value = null
    }
    

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


	// 디버깅/시연을 위한 더미 데이터 주입
	fun loadDummyMeetingInProgressState() {
		val dummyStart = System.currentTimeMillis() - 15 * 60 * 1000 // 15분 전에 시작
		val dummyAgendas = listOf(
			com.imhungry.sillok.domain.model.agenda.Agenda(1L, "프로젝트 소개", true),
			com.imhungry.sillok.domain.model.agenda.Agenda(2L, "요구사항 논의", false),
			com.imhungry.sillok.domain.model.agenda.Agenda(3L, "액션 아이템 정리", false)
		)

		val dummySegments = listOf(
			SegmentUi(timestamp = "00:01:10", text = "안녕하세요, 오늘 아젠다는...", nickname = "홍길동", profileImage = "", isFromCurrentUser = false, isSameAsPrevious = false, isSameAsNext = true),
			SegmentUi(timestamp = "00:01:35", text = "첫 번째로 목표 범위를 정하면...", nickname = "홍길동", profileImage = "", isFromCurrentUser = false, isSameAsPrevious = true, isSameAsNext = false),
			SegmentUi(timestamp = "00:02:10", text = "디자인 관점에서 보면...", nickname = "김디자", profileImage = "", isFromCurrentUser = false, isSameAsPrevious = false, isSameAsNext = false),
			SegmentUi(timestamp = "00:03:45", text = "백엔드 API는...", nickname = "이개발", profileImage = "", isFromCurrentUser = true, isSameAsPrevious = false, isSameAsNext = false),
            SegmentUi(timestamp = "00:01:10", text = "안녕하세요, 오늘 아젠다는...", nickname = "홍길동", profileImage = "", isFromCurrentUser = false, isSameAsPrevious = false, isSameAsNext = true),
            SegmentUi(timestamp = "00:01:35", text = "첫 번째로 목표 범위를 정하면...", nickname = "홍길동", profileImage = "", isFromCurrentUser = false, isSameAsPrevious = true, isSameAsNext = false),
            SegmentUi(timestamp = "00:02:10", text = "디자인 관점에서 보면...", nickname = "김디자", profileImage = "", isFromCurrentUser = false, isSameAsPrevious = false, isSameAsNext = false),
            SegmentUi(timestamp = "00:03:45", text = "백엔드 API는...", nickname = "이개발", profileImage = "", isFromCurrentUser = true, isSameAsPrevious = false, isSameAsNext = false),
            SegmentUi(timestamp = "00:01:10", text = "안녕하세요, 오늘 아젠다는...", nickname = "홍길동", profileImage = "", isFromCurrentUser = false, isSameAsPrevious = false, isSameAsNext = true),
            SegmentUi(timestamp = "00:01:35", text = "첫 번째로 목표 범위를 정하면...", nickname = "홍길동", profileImage = "", isFromCurrentUser = false, isSameAsPrevious = true, isSameAsNext = false),
            SegmentUi(timestamp = "00:02:10", text = "디자인 관점에서 보면...", nickname = "김디자", profileImage = "", isFromCurrentUser = false, isSameAsPrevious = false, isSameAsNext = false),
            SegmentUi(timestamp = "00:03:45", text = "백엔드 API는...", nickname = "이개발", profileImage = "", isFromCurrentUser = true, isSameAsPrevious = false, isSameAsNext = false),
            SegmentUi(timestamp = "00:01:10", text = "안녕하세요, 오늘 아젠다는...", nickname = "홍길동", profileImage = "", isFromCurrentUser = false, isSameAsPrevious = false, isSameAsNext = true),
            SegmentUi(timestamp = "00:01:35", text = "첫 번째로 목표 범위를 정하면...", nickname = "홍길동", profileImage = "", isFromCurrentUser = false, isSameAsPrevious = true, isSameAsNext = false),
            SegmentUi(timestamp = "00:02:10", text = "디자인 관점에서 보면...", nickname = "김디자", profileImage = "", isFromCurrentUser = false, isSameAsPrevious = false, isSameAsNext = false),
            SegmentUi(timestamp = "00:03:45", text = "백엔드 API는...", nickname = "이개발", profileImage = "", isFromCurrentUser = true, isSameAsPrevious = false, isSameAsNext = false),
            SegmentUi(timestamp = "00:01:10", text = "안녕하세요, 오늘 아젠다는...", nickname = "홍길동", profileImage = "", isFromCurrentUser = false, isSameAsPrevious = false, isSameAsNext = true),
            SegmentUi(timestamp = "00:01:35", text = "첫 번째로 목표 범위를 정하면...", nickname = "홍길동", profileImage = "", isFromCurrentUser = false, isSameAsPrevious = true, isSameAsNext = false),
            SegmentUi(timestamp = "00:02:10", text = "디자인 관점에서 보면...", nickname = "김디자", profileImage = "", isFromCurrentUser = false, isSameAsPrevious = false, isSameAsNext = false),
            SegmentUi(timestamp = "00:03:45", text = "백엔드 API는...", nickname = "이개발", profileImage = "", isFromCurrentUser = true, isSameAsPrevious = false, isSameAsNext = false),
            SegmentUi(timestamp = "00:01:10", text = "안녕하세요, 오늘 아젠다는...", nickname = "홍길동", profileImage = "", isFromCurrentUser = false, isSameAsPrevious = false, isSameAsNext = true),
            SegmentUi(timestamp = "00:01:35", text = "첫 번째로 목표 범위를 정하면...", nickname = "홍길동", profileImage = "", isFromCurrentUser = false, isSameAsPrevious = true, isSameAsNext = false),
            SegmentUi(timestamp = "00:02:10", text = "디자인 관점에서 보면...", nickname = "김디자", profileImage = "", isFromCurrentUser = false, isSameAsPrevious = false, isSameAsNext = false),
            SegmentUi(timestamp = "00:03:45", text = "백엔드 API는...", nickname = "이개발", profileImage = "", isFromCurrentUser = true, isSameAsPrevious = false, isSameAsNext = false),
            SegmentUi(timestamp = "00:01:10", text = "안녕하세요, 오늘 아젠다는...", nickname = "홍길동", profileImage = "", isFromCurrentUser = false, isSameAsPrevious = false, isSameAsNext = true),
            SegmentUi(timestamp = "00:01:35", text = "첫 번째로 목표 범위를 정하면...", nickname = "홍길동", profileImage = "", isFromCurrentUser = false, isSameAsPrevious = true, isSameAsNext = false),
            SegmentUi(timestamp = "00:02:10", text = "디자인 관점에서 보면...", nickname = "김디자", profileImage = "", isFromCurrentUser = false, isSameAsPrevious = false, isSameAsNext = false),
            SegmentUi(timestamp = "00:03:45", text = "백엔드 API는...", nickname = "이개발", profileImage = "", isFromCurrentUser = true, isSameAsPrevious = false, isSameAsNext = false)
		)

		val dummySummaries = listOf(
			SummaryUi(content = "회의 목적과 범위를 합의함", timestamp = "00:10:00"),
			SummaryUi(content = "핵심 액션 아이템 3개 도출했고 길게 작성했을 때는 이런 모습이고, 가로 너비는 고정되어 있으니 아래로 길어짐.", timestamp = "00:12:30"),
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
			UserParticipationRate(userId = 1L, nickname = "김부장", rate = 0.45),
			UserParticipationRate(userId = 2L, nickname = "조사원", rate = 0.35),
			UserParticipationRate(userId = 3L, nickname = "정대리", rate = 0.15),
            UserParticipationRate(userId = 4L, nickname = "김상병", rate = 0.08),
            UserParticipationRate(userId = 5L, nickname = "정과장", rate = 0.07)
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