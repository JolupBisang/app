package com.imhungry.sillok.presentation.viewmodel.meeting

import android.app.Application
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.imhungry.sillok.BuildConfig
import com.imhungry.sillok.data.local.FeedbackReadStore
import com.imhungry.sillok.data.local.GeneratingMeetingNoteStore
import com.imhungry.sillok.data.local.TokenStore
import com.imhungry.sillok.data.model.meeting.TargetMeetingStatus
import com.imhungry.sillok.data.model.realtime.ErrorResponse
import com.imhungry.sillok.data.model.realtime.LiveFeedbackDto
import com.imhungry.sillok.data.model.realtime.LiveSummaryDto
import com.imhungry.sillok.data.model.realtime.RealtimeSegmentDto
import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.model.meeting.Meeting
import com.imhungry.sillok.domain.model.participation.UserParticipationRate
import com.imhungry.sillok.domain.usecase.agenda.ChangeAgendaStatusUseCase
import com.imhungry.sillok.domain.usecase.feedback.GetFeedbacksUseCase
import com.imhungry.sillok.domain.usecase.meeting.GetMeetingDetailUseCase
import com.imhungry.sillok.domain.usecase.meeting.UpdateMeetingStatusUseCase
import com.imhungry.sillok.domain.usecase.participation.GetParticipationRateHistoryUseCase
import com.imhungry.sillok.domain.usecase.segment.GetSegmentsUseCase
import com.imhungry.sillok.domain.usecase.summary.GetSummariesUseCase
import com.imhungry.sillok.domain.usecase.user.GetMyProfileUseCase
import com.imhungry.sillok.domain.usecase.user.GetUserInfoUseCase
import com.imhungry.sillok.presentation.service.MeetingInProgressService
import com.imhungry.sillok.presentation.service.MeetingRealtimeEventSource
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
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MeetingInProgressViewModel @Inject constructor(
    private val changeAgendaStatusUseCase: ChangeAgendaStatusUseCase,
    private val getSegmentsUseCase: GetSegmentsUseCase,
    private val getSummariesUseCase: GetSummariesUseCase,
    private val getParticipationRateHistoryUseCase: GetParticipationRateHistoryUseCase,
    private val getFeedbacksUseCase: GetFeedbacksUseCase,
    private val getMeetingDetailUseCase: GetMeetingDetailUseCase,
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val getMyProfileUseCase: GetMyProfileUseCase,
    private val updateMeetingStatusUseCase: UpdateMeetingStatusUseCase,
    private val tokenStore: TokenStore,
    private val feedbackReadStore: FeedbackReadStore,
    private val generatingMeetingNoteStore: GeneratingMeetingNoteStore,
    private val meetingRealtimeEventSource: MeetingRealtimeEventSource,
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
    private val userProfileImageCache = mutableMapOf<Long, String>()

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
            _state.update { it.copy(isLoading = true) }
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

            // Service 이벤트 구독 (start() 호출 전에 구독 시작하여 이벤트를 놓치지 않도록)
            Log.d(TAG, "[4단계] Service 이벤트 구독 시작")
            observeServiceEvents()
            Log.d(TAG, "[4단계 완료] Service 이벤트 구독 완료")

            // 실시간 이벤트 소스 시작 (Real: Service + WebSocket/SSE, Debug: Fake)
            Log.d(TAG, "[5단계] MeetingRealtimeEventSource.start 호출")
            meetingRealtimeEventSource.start(
                serverUrl = BuildConfig.BASE_URL,
                meetingId = meetingId,
                jwtToken = jwtToken
            )
            _state.update { it.copy(isLoading = false) }
        }
    }


    /**
     * MeetingRealtimeEventSource 가 발행하는 ServiceEvent 구독
     * (실제 환경: Service → WebSocket/SSE → ServiceEvent
     *  디버그 환경: FakeEventSource → ServiceEvent)
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun observeServiceEvents() {
        viewModelScope.launch {
            meetingRealtimeEventSource.events.collectLatest { event ->
                Log.d(TAG, "[ServiceEvent] 수신: ${event::class.simpleName}")
                when (event) {
                    is ServiceEvent.ConnectionEstablished -> {
                        Log.d(
                            TAG,
                            "[5-3] ConnectionEstablished 이벤트 처리 시작: actualStartTime=${event.actualStartTime}"
                        )
                        handleConnectionEstablishedFromService(
                            event.actualStartTime
                        )
                    }

                    is ServiceEvent.DiarizedSegment -> handleDiarizedSegment(event.segment)
                    is ServiceEvent.CompletionScheduled -> handleCompletionScheduled()
                    is ServiceEvent.MeetingCompleted -> handleMeetingCompleted(event.message)
                    is ServiceEvent.ParticipationRate -> handleParticipationRate(event.rates)
                    is ServiceEvent.Feedback -> handleFeedback(event.feedback)
                    is ServiceEvent.Summary -> handleSummary(event.summary)
                    is ServiceEvent.Error -> handleError(event.error)
                    is ServiceEvent.AgendaUpdated -> handleAgendaUpdated(event.agendaId, event.isCompleted)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleConnectionEstablishedFromService(
        actualStartTime: String
    ) {
        Log.d(TAG, "========================================")
        Log.d(TAG, "[6단계] 연결 확립 처리 시작")
        Log.d(TAG, "  - actualStartTime: $actualStartTime")
        Log.d(TAG, "========================================")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val startMillis = if (!actualStartTime.isNullOrBlank()) {
                    Log.d(TAG, "[6-1] actualStartTime 원본 값: $actualStartTime")
                    val calculated = DateTimeUtils.isoLocalDateTimeToMillis(actualStartTime)
                    val startTimeFormatted = DateTimeUtils.millisToHourMinute(calculated)
                    Log.d(TAG, "[6-1-1] startMillis 계산 결과: $calculated (${startTimeFormatted})")
                    Log.d(TAG, "[6-1-2] 현재 시간: ${System.currentTimeMillis()} (${DateTimeUtils.millisToHourMinute(System.currentTimeMillis())})")
                    calculated
                } else {
                    Log.w(TAG, "[6-1 경고] actualStartTime이 null이거나 비어있습니다. 현재 시간을 사용합니다.")
                    val currentTime = System.currentTimeMillis()
                    Log.d(TAG, "[6-1-1] 현재 시간을 startMillis로 사용: $currentTime (${DateTimeUtils.millisToHourMinute(currentTime)})")
                    currentTime
                }

                // state 업데이트 (targetTime, restInterval, restDuration은 refreshAll()에서 이미 로드됨)
                withContext(Dispatchers.Main) {
                    Log.d(TAG, "[6-2] State 업데이트 시작")
                    _state.update { 
                        it.copy(startTime = startMillis)
                    }

                    // 휴식 시간 피드백 스케줄링
                    val currentState = state.value
                    if (currentState.targetTime > 0 && currentState.restInterval > 0 && currentState.restDuration > 0) {
                        Log.d(
                            TAG,
                            "[6-3] 휴식 시간 피드백 스케줄링 시작: targetTime=${currentState.targetTime}분, restInterval=${currentState.restInterval}분, restDuration=${currentState.restDuration}분"
                        )
                        scheduleRestBreakFeedbacks(
                            startMillis,
                            currentState.targetTime,
                            currentState.restInterval,
                            currentState.restDuration
                        )
                        Log.d(TAG, "[6-3 완료] 휴식 시간 피드백 스케줄링 완료")
                    } else {
                        Log.d(TAG, "[6-3 스킵] 휴식 시간 설정이 없어 스케줄링을 건너뜁니다 (targetTime=${currentState.targetTime}, restInterval=${currentState.restInterval}, restDuration=${currentState.restDuration})")
                    }

                    // 회의 종료 10분 전 피드백 스케줄링
                    if (currentState.targetTime > 0) {
                        Log.d(
                            TAG,
                            "[6-4] 회의 종료 피드백 스케줄링 시작: targetTime=${currentState.targetTime}분"
                        )
                        scheduleMeetingEndFeedback(startMillis, currentState.targetTime)
                        Log.d(TAG, "[6-4 완료] 회의 종료 피드백 스케줄링 완료")
                    } else {
                        Log.d(TAG, "[6-4 스킵] 목표 시간이 없어 스케줄링을 건너뜁니다 (targetTime=${currentState.targetTime})")
                    }
                    Log.d(TAG, "[6-2 완료] State 업데이트 완료")
                }

                Log.d(TAG, "========================================")
                Log.d(TAG, "[6단계 완료] 연결 확립 처리 완료")
                Log.d(TAG, "========================================")
            } catch (e: Exception) {
                Log.e(TAG, "[6단계 실패] 연결 확립 처리 중 오류: ${e.message}", e)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun refreshAll() {
        Log.d(TAG, "[2-1] refreshAll 시작: isLoading=true")

        val meetingId = state.value.meetingId
        Log.d(TAG, "[2-2] meetingId: $meetingId")

        coroutineScope {
            val meeting = loadMeetingDetail(meetingId) ?: return@coroutineScope
            // 참가자 정보를 먼저 로드하고 완료될 때까지 대기
            loadUserInfoForParticipants(meeting.participants)
            
            val startMillis = updateStartTimeIfAvailable(meeting)
            if (startMillis != null) {
                //loadMeetingData(meetingId, startMillis)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun loadMeetingDetail(meetingId: Long): Meeting? {
        Log.d(TAG, "[2-3] Meeting Detail 조회 시작")
        return when (val result = getMeetingDetailUseCase(meetingId)) {
            is ApiResult.Success -> {
                val meeting = result.data
                Log.d(
                    TAG,
                    "[2-3 완료] Meeting Detail 조회 성공: targetTime=${meeting.targetTime}, restInterval=${meeting.restInterval}, restDuration=${meeting.restDuration}, participants=${meeting.participants.size}명"
                )
                _state.update {
                    it.copy(
                        targetTime = meeting.targetTime,
                        restInterval = meeting.restInterval,
                        restDuration = meeting.restDuration,
                        isHost = meeting.isHost,
                        agendas = meeting.agendas
                    )
                }
                meeting
            }
            is ApiResult.Failure -> {
                Log.e(TAG, "[2-3 실패] Meeting Detail 조회 실패: ${result.message}")
                null
            }
        }
    }

    private suspend fun loadUserInfoForParticipants(participants: List<Meeting.Participant>) {
        coroutineScope {
            participants.forEach { participant ->
                launch(Dispatchers.IO) {
                    try {
                        when (val userResult = getUserInfoUseCase(participant.email)) {
                            is ApiResult.Success -> {
                                userNicknameCache[participant.userId] = userResult.data.nickname
                                userProfileImageCache[participant.userId] = userResult.data.pictureURL
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
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun updateStartTimeIfAvailable(meeting: Meeting): Long? {
        return meeting.actualStartTime?.let { actualStartTime ->
            val startMillis = DateTimeUtils.isoLocalDateTimeToMillis(actualStartTime)
            _state.update { it.copy(startTime = startMillis) }
            startMillis
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun loadMeetingData(meetingId: Long, startMillis: Long) {
        coroutineScope {
            val currentUserId = when (val result = getMyProfileUseCase()) {
                is ApiResult.Success -> result.data.id
                is ApiResult.Failure -> {
                    Log.e(TAG, "현재 사용자 정보 조회 실패: ${result.message}")
                    null
                }
            }
            //val segmentsDeferred = async { getSegmentsUseCase(meetingId, page = 0, size = 10000) }
            val summariesDeferred = async { getSummariesUseCase(meetingId, isRecap = false, page = 0, size = 10000) }
            val participationDeferred = async { getParticipationRateHistoryUseCase(meetingId) }
            val feedbacksDeferred = async { getFeedbacksUseCase(meetingId, page = 0, size = 10000) }

            //loadSegments(segmentsDeferred.await(), startMillis, currentUserId)
            loadSummaries(summariesDeferred.await(), startMillis)
            // 참가자 정보가 로드된 후 participation rate 로드
            loadParticipationRates(participationDeferred.await())
            loadFeedbacks(feedbacksDeferred.await(), meetingId, startMillis)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadSegments(
        result: ApiResult<List<com.imhungry.sillok.domain.model.segment.Segment>>,
        startMillis: Long,
        currentUserId: Long?
    ) {
        when (result) {
            is ApiResult.Success -> {
                val ui = result.data.mapIndexed { index, seg ->
                    val prevUserId = if (index > 0) result.data[index - 1].userId else null
                    val nextUserId = if (index < result.data.lastIndex) result.data[index + 1].userId else null
                    val isSameAsPrevious = prevUserId != null && prevUserId == seg.userId
                    val isSameAsNext = nextUserId != null && nextUserId == seg.userId
                    val millis = DateTimeUtils.isoLocalDateTimeToMillis(seg.timestamp)
                    SegmentUi(
                        order = seg.segmentOrder,
                        timestamp = DateTimeUtils.getElapsedStringFromMillis(startMillis, millis),
                        text = seg.text,
                        nickname = userNicknameCache[seg.userId] ?: "사용자 ${seg.userId}",
                        profileImage = userProfileImageCache[seg.userId] ?: "",
                        isFromCurrentUser = currentUserId != null && seg.userId == currentUserId,
                        isSameAsPrevious = isSameAsPrevious,
                        isSameAsNext = isSameAsNext
                    )
                }
                _state.update { it.copy(segments = ui) }
            }
            is ApiResult.Failure -> Log.e(TAG, "세그먼트 로드 실패: ${result.message}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadSummaries(
        result: ApiResult<List<com.imhungry.sillok.domain.model.summary.Summary>>,
        startMillis: Long
    ) {
        when (result) {
            is ApiResult.Success -> {
                val ui = result.data.map {
                    val millis = DateTimeUtils.isoLocalDateTimeToMillis(it.generatedDateTime)
                    SummaryUi(
                        content = it.content,
                        timestamp = DateTimeUtils.getElapsedStringFromMillis(startMillis, millis)
                    )
                }
                _state.update { it.copy(summaries = ui) }
            }
            is ApiResult.Failure -> Log.e(TAG, "요약 로드 실패: ${result.message}")
        }
    }

    private fun loadParticipationRates(
        result: ApiResult<List<UserParticipationRate>>
    ) {
        when (result) {
            is ApiResult.Success -> {
                // API에서 가져온 참여율이 있으면 사용
                if (result.data.isNotEmpty()) {
                    val sorted = result.data.sortedByDescending { it.rate }
                    _state.update { it.copy(participationRates = sorted) }
                } else {
                    // 참여율이 없으면 참가자 정보를 기반으로 초기값(0.0) 설정
                    initializeParticipationRatesFromParticipants()
                }
            }
            is ApiResult.Failure -> {
                Log.e(TAG, "참여율 로드 실패: ${result.message}")
                // 실패 시에도 참가자 정보를 기반으로 초기값 설정
                initializeParticipationRatesFromParticipants()
            }
        }
    }
    
    /**
     * 회의 참가자 정보를 기반으로 초기 participation rate 설정 (모두 0.0)
     */
    private fun initializeParticipationRatesFromParticipants() {
        val currentState = state.value
        // 참가자 정보는 이미 loadUserInfoForParticipants에서 userNicknameCache에 저장되어 있음
        val initialRates = userNicknameCache.map { (userId, nickname) ->
            UserParticipationRate(
                userId = userId,
                nickname = nickname,
                rate = 0.0
            )
        }.sortedByDescending { it.rate }
        
        if (initialRates.isNotEmpty()) {
            _state.update { it.copy(participationRates = initialRates) }
            Log.d(TAG, "참가자 기반 초기 참여율 설정: ${initialRates.size}명")
        } else {
            Log.w(TAG, "참가자 정보가 없어 초기 참여율을 설정할 수 없습니다")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun loadFeedbacks(
        result: ApiResult<List<com.imhungry.sillok.domain.model.feedback.Feedback>>,
        meetingId: Long,
        startMillis: Long
    ) {
        when (result) {
            is ApiResult.Success -> {
                val ui = result.data.map {
                    val millis = DateTimeUtils.isoLocalDateTimeToMillis(it.generatedDateTime)
                    FeedbackUi(
                        comment = it.comment,
                        timestamp = DateTimeUtils.getElapsedStringFromMillis(startMillis, millis),
                        isRead = false
                    )
                }

                val lastReadIndex = feedbackReadStore.getLastReadFeedbackIndex(meetingId)
                val finalUi = if (lastReadIndex != null && lastReadIndex >= 0) {
                    ui.mapIndexed { index, feedback ->
                        if (index <= lastReadIndex) feedback.copy(isRead = true) else feedback
                    }
                } else {
                    ui
                }

                _state.update { it.copy(feedbacks = finalUi) }
            }
            is ApiResult.Failure -> Log.e(TAG, "피드백 로드 실패: ${result.message}")
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
            when (val result =
                updateMeetingStatusUseCase(meetingId, TargetMeetingStatus.COMPLETED)) {
                is ApiResult.Success -> {
                    Log.d(TAG, "회의 완료 처리 성공: meetingId=$meetingId")
                }

                is ApiResult.Failure -> {
                    Log.e(TAG, "회의 완료 처리 실패: ${result.message}")
                    _state.update { it.copy(error = result.message) }
                }
            }
        }
    }

    /**
     * 마이크 토글 → EventSource 통해 Service 로 전달
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun toggleMic() {
        val wasEnabled = _micEnabled.value
        val newState = !wasEnabled
        meetingRealtimeEventSource.toggleMic()
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

        // state에 세그먼트 추가
        viewModelScope.launch {
            val currentState = state.value
            val startMillis = currentState.startTime
            val currentUserId = when (val result = getMyProfileUseCase()) {
                is ApiResult.Success -> result.data.id
                is ApiResult.Failure -> {
                    Log.e(TAG, "현재 사용자 정보 조회 실패: ${result.message}")
                    null
                }
            }

            if (startMillis > 0) {
                val currentSegments = currentState.segments.toMutableList()
                
                // 같은 order를 가진 기존 세그먼트가 있으면 제거 (덮어쓰기)
                val existingIndex = currentSegments.indexOfFirst { it.order == data.order }
                if (existingIndex != -1) {
                    Log.d(TAG, "[handleDiarizedSegment] 같은 order(${data.order})의 기존 세그먼트 제거: index=$existingIndex")
                    currentSegments.removeAt(existingIndex)
                }

                val lastSegment = currentSegments.lastOrNull()

                // 캐시에서 nickname 가져오기 (없으면 기본값 사용)
                val nickname = userNicknameCache[data.userId] ?: "사용자 ${data.userId}"
                val profileImage = userProfileImageCache[data.userId] ?: ""
                val isSameAsPrevious = lastSegment != null && lastSegment.nickname == nickname

                val millis = DateTimeUtils.isoLocalDateTimeToMillis(data.timestamp)

                val segmentUi = SegmentUi(
                    order = data.order,
                    timestamp = DateTimeUtils.getElapsedStringFromMillis(startMillis, millis),
                    text = data.text,
                    nickname = nickname,
                    profileImage = profileImage,
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

                // DataStore에 generatingMeetingNoteId 저장
                generatingMeetingNoteStore.setGeneratingMeetingNoteId(meetingId)
                Log.d(TAG, "DataStore generatingMeetingNoteId 저장 완료: meetingId=$meetingId")

                // 홈으로 이동 이벤트 발생
                _events.emit(MeetingInProgressEvent.NavigateToHome)
            } catch (e: Exception) {
                Log.e(TAG, "DataStore 저장 실패: ${e.message}", e)
            }
        }
    }

    /**
     * MEETING_COMPLETED 수신 시 처리
     * Service에서 이미 clearGeneratingMeetingNoteId를 처리하므로 여기서는 연결 해제만 수행
     * (ViewModel이 파괴되었을 수 있으므로 Service에서 직접 처리)
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleMeetingCompleted(message: String?) {
        Log.d(TAG, "========================================")
        Log.d(TAG, "회의록 생성 완료: $message")
        Log.d(TAG, "========================================")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // clearGeneratingMeetingNoteId는 Service에서 처리됨 (ViewModel이 파괴되었을 수 있으므로)
                // 여기서는 연결 해제만 수행
                meetingRealtimeEventSource.stop()
                Log.d(TAG, "회의록 생성 완료 후 연결 해제 완료 (DataStore 업데이트는 Service에서 처리됨)")
            } catch (e: Exception) {
                Log.e(TAG, "연결 해제 실패: ${e.message}", e)
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
                    nickname = userNicknameCache[userId] ?: "사용자 $userId",
                    rate = rate,
                )
            }
            
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
                val currentTime = DateTimeUtils.getCurrentTime()
                val millis = DateTimeUtils.isoLocalDateTimeToMillis(currentTime)

                val feedbackUi = FeedbackUi(
                    comment = feedback.comment,
                    timestamp = DateTimeUtils.getElapsedStringFromMillis(startMillis, millis),
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
                val currentTime = DateTimeUtils.getCurrentTime()
                val millis = DateTimeUtils.isoLocalDateTimeToMillis(currentTime)

                val summaryUi = SummaryUi(
                    content = summary.summary,
                    timestamp = DateTimeUtils.getElapsedStringFromMillis(startMillis, millis)
                )

                currentSummaries.add(summaryUi)

                _state.update { it.copy(summaries = currentSummaries) }
            }
        }
    }

    // ========================================
    // 연결 해제
    // ========================================

    /**
     * 모든 연결 해제 (Service / WebSocket / SSE)
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun disconnectAll() {
        viewModelScope.launch {
            meetingRealtimeEventSource.stop()
            Log.d(TAG, "모든 연결 해제 완료")
        }
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
    private fun scheduleRestBreakFeedbacks(
        startMillis: Long,
        targetTime: Int,
        restInterval: Int,
        restDuration: Int
    ) {
        Log.d(TAG, "[scheduleRestBreakFeedbacks] 호출됨")
        Log.d(TAG, "  - startMillis: $startMillis")
        Log.d(TAG, "  - targetTime: $targetTime")
        Log.d(TAG, "  - restInterval: $restInterval")
        Log.d(TAG, "  - restDuration: $restDuration")
        
        if (restInterval <= 0 || restDuration <= 0) {
            Log.w(TAG, "[scheduleRestBreakFeedbacks] 스킵: restInterval=$restInterval, restDuration=$restDuration")
            return
        }

        // 기존 스케줄링 Job 취소 (중복 방지)
        restBreakSchedulingJob?.cancel()
        Log.d(TAG, "[scheduleRestBreakFeedbacks] 기존 Job 취소 완료")

        // 쉬는 시간 범위 계산 및 저장
        viewModelScope.launch {
            val targetMillis = targetTime * 60 * 1000L
            val endMillis = startMillis + targetMillis
            var restStartMillis = startMillis + restInterval * 60 * 1000L // 첫 번째 휴식 시작 시간
            val restBreakPeriodsList = mutableListOf<Pair<String, String>>()
            
            Log.d(TAG, "[휴식 시간 계산] 시작")
            Log.d(TAG, "  - 회의 시작 시간: ${DateTimeUtils.millisToHourMinute(startMillis)} ($startMillis)")
            Log.d(TAG, "  - 회의 종료 시간: ${DateTimeUtils.millisToHourMinute(endMillis)} ($endMillis)")
            Log.d(TAG, "  - 목표 시간: ${targetTime}분 (${targetMillis}ms)")
            Log.d(TAG, "  - 휴식 간격: ${restInterval}분")
            Log.d(TAG, "  - 휴식 지속 시간: ${restDuration}분")

            var restCount = 0
            while (restStartMillis < endMillis) {
                restCount++
                val restEndMillis = restStartMillis + restDuration * 60 * 1000L // 휴식 종료 시간

                // 쉬는 시간 범위를 경과 시간 문자열로 변환
                val restStartElapsed =
                    DateTimeUtils.getElapsedStringFromMillis(startMillis, restStartMillis)
                val restEndElapsed =
                    DateTimeUtils.getElapsedStringFromMillis(startMillis, restEndMillis)
                
                val restStartTime = DateTimeUtils.millisToHourMinute(restStartMillis)
                val restEndTime = DateTimeUtils.millisToHourMinute(restEndMillis)

                Log.d(TAG, "  [휴식 #$restCount]")
                Log.d(TAG, "    - 시작: $restStartTime ($restStartMillis) - 경과: $restStartElapsed")
                Log.d(TAG, "    - 종료: $restEndTime ($restEndMillis) - 경과: $restEndElapsed")

                restBreakPeriodsList.add(Pair(restStartElapsed, restEndElapsed))

                // 다음 휴식 시간으로 이동
                restStartMillis += restInterval * 60 * 1000L
            }
            
            Log.d(TAG, "[휴식 시간 계산] 완료: 총 ${restCount}개의 휴식 시간 계산됨")

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
                    val feedbackTimestamp =
                        DateTimeUtils.getElapsedStringFromMillis(startMillis, feedbackTimeMillis)

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
        Log.d(TAG, "[scheduleMeetingEndFeedback] 호출됨")
        Log.d(TAG, "  - startMillis: $startMillis")
        Log.d(TAG, "  - targetTime: $targetTime")
        
        if (targetTime <= 0) {
            Log.w(TAG, "[scheduleMeetingEndFeedback] 스킵: targetTime=$targetTime")
            return
        }

        // 기존 스케줄링 Job 취소 (중복 방지)
        meetingEndSchedulingJob?.cancel()
        Log.d(TAG, "[scheduleMeetingEndFeedback] 기존 Job 취소 완료")

        meetingEndSchedulingJob = viewModelScope.launch(Dispatchers.IO) {
            // 회의 종료 시간 계산
            val endMillis = startMillis + targetTime * 60 * 1000L
            val feedbackTimeMillis = endMillis - 10 * 60 * 1000L // 종료 10분 전
            
            Log.d(TAG, "[회의 종료 시간 계산]")
            Log.d(TAG, "  - 회의 시작 시간: ${DateTimeUtils.millisToHourMinute(startMillis)} ($startMillis)")
            Log.d(TAG, "  - 목표 시간: ${targetTime}분 (${targetTime * 60 * 1000L}ms)")
            Log.d(TAG, "  - 회의 종료 시간: ${DateTimeUtils.millisToHourMinute(endMillis)} ($endMillis)")
            Log.d(TAG, "  - 피드백 알림 시간 (종료 10분 전): ${DateTimeUtils.millisToHourMinute(feedbackTimeMillis)} ($feedbackTimeMillis)")
            Log.d(TAG, "  - 현재 시간: ${DateTimeUtils.millisToHourMinute(System.currentTimeMillis())} (${System.currentTimeMillis()})")

            // 현재 시간 이후의 피드백만 처리
            val delayMillis = feedbackTimeMillis - System.currentTimeMillis()
            Log.d(TAG, "  - 피드백까지 남은 시간: ${delayMillis / 1000 / 60}분 ${(delayMillis / 1000) % 60}초 (${delayMillis}ms)")
            if (delayMillis > 0) {
                delay(delayMillis)

                // 종료 예정 시각 포맷팅 (HH:MM 형식)
                val endTime = DateTimeUtils.millisToHourMinute(endMillis)

                val feedbackMessage = "회의 종료까지 10분 남았습니다.\n예정 종료 시각: $endTime"
                val feedbackTimestamp =
                    DateTimeUtils.getElapsedStringFromMillis(startMillis, feedbackTimeMillis)

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
}