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
import com.imhungry.sillok.data.model.meeting.TargetMeetingStatus
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
import com.imhungry.sillok.presentation.service.ServiceEvent
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
        Log.d(TAG, "========================================")
        Log.d(TAG, "[1단계] 회의 초기화 시작: meetingId=$meetingId")
        Log.d(TAG, "========================================")
        _state.update { it.copy(meetingId = meetingId) }
        viewModelScope.launch {
            // refreshAll() 완료 후 Service 시작
            Log.d(TAG, "[2단계] 회의 데이터 로드 시작 (refreshAll)")
            refreshAll()
            Log.d(TAG, "[2단계 완료] 회의 데이터 로드 완료")

            // TokenStore에서 토큰 가져오기
            Log.d(TAG, "[3단계] JWT 토큰 조회 시작")
            val jwtToken = tokenStore.accessToken.first()
            if (jwtToken == null) {
                Log.e(TAG, "[3단계 실패] JWT 토큰이 null입니다")
                return@launch
            }
            Log.d(TAG, "[3단계 완료] JWT 토큰 조회 완료")
            
            // Service 시작
            Log.d(TAG, "[4단계] Service 시작 요청")
            startService(
                serverUrl = BuildConfig.BASE_URL,
                meetingId = meetingId,
                jwtToken = jwtToken
            )
            
            // Service 이벤트 구독
            Log.d(TAG, "[5단계] Service 이벤트 구독 시작")
            observeServiceEvents()
            Log.d(TAG, "[5단계 완료] Service 이벤트 구독 완료")
        }
    }
    
    /**
     * Service 시작
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun startService(serverUrl: String, meetingId: Long, jwtToken: String) {
        Log.d(TAG, "[4-1] Service Intent 생성: serverUrl=$serverUrl, meetingId=$meetingId")
        val intent = Intent(app, MeetingInProgressService::class.java).apply {
            action = MeetingInProgressService.ACTION_START
            putExtra(MeetingInProgressService.EXTRA_SERVER_URL, serverUrl)
            putExtra(MeetingInProgressService.EXTRA_MEETING_ID, meetingId)
            putExtra(MeetingInProgressService.EXTRA_JWT_TOKEN, jwtToken)
        }
        ContextCompat.startForegroundService(app, intent)
        Log.d(TAG, "[4-2] Foreground Service 시작 요청 완료: meetingId=$meetingId")
    }
    
    /**
     * Service 이벤트 구독
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun observeServiceEvents() {
        viewModelScope.launch {
            Log.d(TAG, "[5-1] Service 이벤트 Flow 구독 시작")
            MeetingInProgressService.serviceEvents.collectLatest { event ->
                Log.d(TAG, "[5-2] Service 이벤트 수신: ${event::class.simpleName}")
                when (event) {
                    is ServiceEvent.ConnectionEstablished -> {
                        Log.d(TAG, "[5-3] ConnectionEstablished 이벤트 처리 시작: lastProcessedChunkId=${event.lastProcessedChunkId}")
                        _state.update { it.copy(isLoading = false) }
                        handleConnectionEstablishedFromService(event.lastProcessedChunkId)
                    }
                    is ServiceEvent.DiarizedSegment -> {
                        handleDiarizedSegment(event.segment)
                    }
                    is ServiceEvent.CompletionScheduled -> {
                        handleCompletionScheduled()
                    }
                    is ServiceEvent.MeetingCompleted -> {
                        handleMeetingCompleted(event.message)
                    }
                    is ServiceEvent.ParticipationRate -> {
                        handleParticipationRate(event.rates)
                    }
                    is ServiceEvent.Feedback -> {
                        handleFeedback(event.feedback)
                    }
                    is ServiceEvent.Summary -> {
                        handleSummary(event.summary)
                    }
                    is ServiceEvent.Error -> {
                        handleError(event.error)
                    }
                    is ServiceEvent.MicEnabled -> {
                        _state.update { it.copy(isMicLoading = false) }
                        Log.d(TAG, "마이크 활성화 완료")
                    }
                    is ServiceEvent.AgendaUpdated -> {
                        handleAgendaUpdated(event.agendaId, event.isCompleted)
                    }
                }
            }
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleConnectionEstablishedFromService(lastProcessedChunkId: Long?) {
        Log.d(TAG, "========================================")
        Log.d(TAG, "[6단계] 연결 확립 처리 시작")
        Log.d(TAG, "  - lastProcessedChunkId: $lastProcessedChunkId")
        Log.d(TAG, "========================================")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val meetingId = state.value.meetingId
                val currentTimeMillis = System.currentTimeMillis()
                Log.d(TAG, "[6-1] 회의 시작 시간 확인: meetingId=$meetingId")
                
                // Firebase에서 기존 startMillis 조회
                try {
                    Log.d(TAG, "[6-2] Firebase startMillis 조회 시작")
                    val snapshot = FirebaseFirestore.getInstance()
                        .collection("meetings")
                        .document(meetingId.toString())
                        .get()
                        .await()
                    val existingStartMillis = snapshot.getLong("startMillis")
                    
                    val startMillisToUse = if (existingStartMillis != null) {
                        Log.d(TAG, "[6-2 완료] Firebase에 이미 startMillis가 존재: $existingStartMillis (업데이트하지 않음)")
                        existingStartMillis
                    } else {
                        Log.d(TAG, "[6-2-1] Firebase에 startMillis가 없음, 새로 저장: $currentTimeMillis")
                        FirebaseFirestore.getInstance()
                            .collection("meetings")
                            .document(meetingId.toString())
                            .update("startMillis", currentTimeMillis)
                            .await()
                        Log.d(TAG, "[6-2 완료] Firebase startMillis 저장 성공: $currentTimeMillis")
                        currentTimeMillis
                    }
                    
                    // state 업데이트
                    withContext(Dispatchers.Main) {
                        Log.d(TAG, "[6-3] State 업데이트 시작")
                        _state.update { it.copy(startTime = startMillisToUse) }
                        
                        // 휴식 시간 피드백 스케줄링
                        val currentState = state.value
                        if (currentState.targetTime > 0 && currentState.restInterval > 0 && currentState.restDuration > 0) {
                            Log.d(TAG, "[6-4] 휴식 시간 피드백 스케줄링 시작: targetTime=${currentState.targetTime}, restInterval=${currentState.restInterval}, restDuration=${currentState.restDuration}")
                            scheduleRestBreakFeedbacks(
                                startMillisToUse,
                                currentState.targetTime,
                                currentState.restInterval,
                                currentState.restDuration
                            )
                            Log.d(TAG, "[6-4 완료] 휴식 시간 피드백 스케줄링 완료")
                        } else {
                            Log.d(TAG, "[6-4 스킵] 휴식 시간 설정이 없어 스케줄링을 건너뜁니다")
                        }
                        
                        // 회의 종료 10분 전 피드백 스케줄링
                        if (currentState.targetTime > 0) {
                            Log.d(TAG, "[6-5] 회의 종료 피드백 스케줄링 시작: targetTime=${currentState.targetTime}")
                            scheduleMeetingEndFeedback(startMillisToUse, currentState.targetTime)
                            Log.d(TAG, "[6-5 완료] 회의 종료 피드백 스케줄링 완료")
                        } else {
                            Log.d(TAG, "[6-5 스킵] 목표 시간이 없어 스케줄링을 건너뜁니다")
                        }
                        Log.d(TAG, "[6-3 완료] State 업데이트 완료")
                    }
                    Log.d(TAG, "========================================")
                    Log.d(TAG, "[6단계 완료] 연결 확립 처리 완료")
                    Log.d(TAG, "========================================")
                } catch (e: Exception) {
                    Log.e(TAG, "[6-2 실패] Firebase startMillis 조회/저장 실패: ${e.message}", e)
                }
            } catch (e: Exception) {
                Log.e(TAG, "[6단계 실패] 연결 확립 처리 중 오류: ${e.message}", e)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun refreshAll() {
        Log.d(TAG, "[2-1] refreshAll 시작: isLoading=true")
        _state.update { it.copy(isLoading = true) }
        val meetingId = state.value.meetingId
        Log.d(TAG, "[2-2] meetingId: $meetingId")

        coroutineScope {
            // 1. Meeting Detail 가져오기 (participants 정보 포함)
            Log.d(TAG, "[2-3] Meeting Detail 조회 시작")
            when (val meetingDetailResult = getMeetingDetailUseCase(meetingId)) {
                is ApiResult.Success -> {
                    val meeting = meetingDetailResult.data
                    Log.d(TAG, "[2-3 완료] Meeting Detail 조회 성공: targetTime=${meeting.targetTime}, restInterval=${meeting.restInterval}, restDuration=${meeting.restDuration}, participants=${meeting.participants.size}명")
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
                    Log.e(TAG, "[2-3 실패] Meeting Detail 조회 실패: ${meetingDetailResult.message}")
                }
            }

            Log.d(TAG, "[2-4] 아젠다 조회 시작 (비동기)")
            val agendasDeferred = async { getAgendasUseCase(meetingId) }

            var startMillis: Long? = null

            Log.d(TAG, "[2-5] Firebase startMillis 조회 시작")
            try {
                val snapshot = FirebaseFirestore.getInstance()
                    .collection("meetings")
                    .document(meetingId.toString())
                    .get()
                    .await()
                startMillis = snapshot.getLong("startMillis")
                Log.d(TAG, "[2-5 완료] Firebase startMillis 조회 완료: $startMillis")
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
				Log.e(TAG, "[2-5 실패] Firebase startMillis 조회 실패: ${e.message}", e)
            }

            Log.d(TAG, "[2-4 대기] 아젠다 조회 결과 대기 중")
            when (val result = agendasDeferred.await()) {
				is ApiResult.Success -> {
                    Log.d(TAG, "[2-4 완료] 아젠다 조회 성공: ${result.data.size}개")
                    _state.update { it.copy(agendas = result.data) }
                }
				is ApiResult.Failure -> Log.e(TAG, "[2-4 실패] 아젠다 조회 실패: ${result.message}")
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
                                nickname = userNicknameCache[seg.userId] ?: "사용자 ${seg.userId}",
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
                                content = it.content.joinToString("\n"),
                                timestamp = DateTimeUtils.getElapsedString(startMillis, it.generatedDateTime)
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
                                timestamp = DateTimeUtils.getElapsedString(startMillis, it.generatedDateTime),
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
            when (val result = updateMeetingStatusUseCase(meetingId, TargetMeetingStatus.COMPLETED)) {
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
        val wasEnabled = _micEnabled.value
        val newState = !wasEnabled
        
        val intent = Intent(app, MeetingInProgressService::class.java).apply {
            action = MeetingInProgressService.ACTION_TOGGLE_MIC
        }
        app.startService(intent)
        
        _micEnabled.value = newState
        
        // 마이크를 켤 때 (꺼져있었다가 켜질 때) 로딩 상태 설정
        if (!wasEnabled && newState) {
            _state.update { it.copy(isMicLoading = true) }
            Log.d(TAG, "마이크 켜는 중... (로딩 시작)")
        } else {
            _state.update { it.copy(isMicLoading = false) }
        }
        
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

    /**
     * 웹소켓에서 받은 아젠다 업데이트 처리
     * 서버에서 이미 처리된 상태이므로 로컬 state만 업데이트
     */
    private fun handleAgendaUpdated(agendaId: Long, isCompleted: Boolean) {
        Log.d(TAG, "아젠다 업데이트 수신: agendaId=$agendaId, isCompleted=$isCompleted")
        val currentAgendas = state.value.agendas
        val updated = currentAgendas.map { agenda ->
            if (agenda.agendaId == agendaId) agenda.copy(isCompleted = isCompleted) else agenda
        }
        _state.update { it.copy(agendas = updated) }
        Log.d(TAG, "아젠다 업데이트 완료: agendaId=$agendaId, isCompleted=$isCompleted")
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
     * COMPLETION_SCHEDULED 수신 시 처리
     * Firebase에 generatingMeetingNoteId 저장
     * (녹음과 SSE 연결은 Service에서 이미 해제됨)
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleCompletionScheduled() {
        Log.d(TAG, "========================================")
        Log.d(TAG, "회의 완료 예약 (회의록 생성 시작)")
        Log.d(TAG, "========================================")
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val meetingId = state.value.meetingId
                
                // Firebase에 generatingMeetingNoteId 저장
                FirebaseFirestore.getInstance()
                    .collection("meetings")
                    .document(meetingId.toString())
                    .update("generatingMeetingNoteId", meetingId)
                    .await()
                
                Log.d(TAG, "Firebase generatingMeetingNoteId 업데이트 완료: meetingId=$meetingId")
                
                // 홈으로 이동 이벤트 발생
                _events.emit(MeetingInProgressEvent.NavigateToHome)
            } catch (e: Exception) {
                Log.e(TAG, "Firebase 업데이트 실패: ${e.message}", e)
            }
        }
    }

    /**
     * MEETING_COMPLETED 수신 시 처리
     * Firebase에 회의록 생성 완료 상태 업데이트 (generatingMeetingNoteId 제거)
     * WebSocket 연결과 Service 종료
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleMeetingCompleted(message: String?) {
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
                
                // WebSocket 연결과 Service 종료
                disconnectAll()
                Log.d(TAG, "회의록 생성 완료 후 연결 해제 및 Service 종료 완료")
            } catch (e: Exception) {
                Log.e(TAG, "Firebase generatingMeetingNoteId 제거 실패: ${e.message}", e)
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
            
            // 새로운 참여율로 업데이트
            val updatedRates = rates.map { (userId, rate) ->
                existingRatesMap[userId]?.let { existing ->
                    existing.copy(rate = rate)
                } ?: UserParticipationRate(
                    userId = userId,
                    rate = rate,
                    totalParticipationChunk = 0L
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
			UserParticipationRate(userId = 1L, rate = 0.45, totalParticipationChunk = 150L),
			UserParticipationRate(userId = 2L, rate = 0.35, totalParticipationChunk = 120L),
			UserParticipationRate(userId = 3L, rate = 0.15, totalParticipationChunk = 50L),
            UserParticipationRate(userId = 4L, rate = 0.08, totalParticipationChunk = 25L),
            UserParticipationRate(userId = 5L, rate = 0.07, totalParticipationChunk = 20L)
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