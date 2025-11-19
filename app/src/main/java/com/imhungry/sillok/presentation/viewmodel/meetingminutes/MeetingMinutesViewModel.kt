package com.imhungry.sillok.presentation.viewmodel.meetingminutes

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.imhungry.sillok.data.paging.FeedbackPagingSource
import com.imhungry.sillok.data.paging.SegmentPagingSource
import com.imhungry.sillok.data.paging.SummaryPagingSource
import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.usecase.audio.GetAudioListUseCase
import com.imhungry.sillok.domain.usecase.feedback.GetFeedbacksUseCase
import com.imhungry.sillok.domain.usecase.meeting.GetMeetingDetailUseCase
import com.imhungry.sillok.domain.usecase.participation.GetParticipationRateHistoryUseCase
import com.imhungry.sillok.domain.usecase.segment.GetSegmentsUseCase
import com.imhungry.sillok.domain.usecase.summary.GetSummariesUseCase
import com.imhungry.sillok.domain.usecase.user.GetMyProfileUseCase
import com.imhungry.sillok.domain.usecase.user.GetUserInfoUseCase
import com.imhungry.sillok.presentation.state.meeting.FeedbackUi
import com.imhungry.sillok.presentation.state.meeting.SegmentUi
import com.imhungry.sillok.presentation.state.meeting.SummaryUi
import com.imhungry.sillok.presentation.state.meetingminutes.MeetingMinutesState
import com.imhungry.sillok.presentation.util.DateTimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MeetingMinutesViewModel @Inject constructor(
    private val getMeetingDetailUseCase: GetMeetingDetailUseCase,
    private val getSegmentsUseCase: GetSegmentsUseCase,
    private val getSummariesUseCase: GetSummariesUseCase,
    private val getParticipationRateHistoryUseCase: GetParticipationRateHistoryUseCase,
    private val getFeedbacksUseCase: GetFeedbacksUseCase,
    private val getAudioListUseCase: GetAudioListUseCase,
    private val getMyProfileUseCase: GetMyProfileUseCase,
    private val getUserInfoUseCase: GetUserInfoUseCase
) : ViewModel() {
    companion object {
        private const val TAG = "MeetingMinutesViewModel"
    }

    private val _state = MutableStateFlow(MeetingMinutesState())
    val state: StateFlow<MeetingMinutesState> = _state.asStateFlow()

    // 사용자 정보 캐시 (userId -> nickname, profileImage)
    private val userNicknameCache = mutableMapOf<Long, String>()
    private val userProfileImageCache = mutableMapOf<Long, String>()
    
    // Paging Flows
    private val _segmentsPagingFlow = MutableStateFlow<Flow<PagingData<com.imhungry.sillok.domain.model.segment.Segment>>?>(null)
    val segmentsPagingFlow: StateFlow<Flow<PagingData<com.imhungry.sillok.domain.model.segment.Segment>>?> = _segmentsPagingFlow.asStateFlow()
    
    private val _summariesPagingFlow = MutableStateFlow<Flow<PagingData<com.imhungry.sillok.domain.model.summary.Summary>>?>(null)
    val summariesPagingFlow: StateFlow<Flow<PagingData<com.imhungry.sillok.domain.model.summary.Summary>>?> = _summariesPagingFlow.asStateFlow()
    
    private val _feedbacksPagingFlow = MutableStateFlow<Flow<PagingData<com.imhungry.sillok.domain.model.feedback.Feedback>>?>(null)
    val feedbacksPagingFlow: StateFlow<Flow<PagingData<com.imhungry.sillok.domain.model.feedback.Feedback>>?> = _feedbacksPagingFlow.asStateFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    fun initialize(meetingId: Long) {
        _state.update { it.copy(meetingId = meetingId) }
        refreshAll()
        //loadDummyMeetingMinutesState()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshAll() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    error = null
                )
            }

            val meetingId = state.value.meetingId

            val detailDeferred = async { getMeetingDetailUseCase(meetingId) }
            val recapDeferred =
                async { getSummariesUseCase(meetingId, isRecap = true, page = 0, size = 1) }
            val participationDeferred = async { getParticipationRateHistoryUseCase(meetingId) }
            val audioDeferred = async { getAudioListUseCase(meetingId) }
            
            // Paging Flows 생성
            val segmentsPagingFlow = Pager(
                config = PagingConfig(
                    pageSize = 500,
                    enablePlaceholders = false
                ),
                pagingSourceFactory = {
                    SegmentPagingSource(
                        getSegmentsUseCase = getSegmentsUseCase,
                        meetingId = meetingId,
                        pageSize = 500
                    )
                }
            ).flow.cachedIn(viewModelScope)
            
            val summariesPagingFlow = Pager(
                config = PagingConfig(
                    pageSize = 500,
                    enablePlaceholders = false
                ),
                pagingSourceFactory = {
                    SummaryPagingSource(
                        getSummariesUseCase = getSummariesUseCase,
                        meetingId = meetingId,
                        isRecap = false,
                        pageSize = 500
                    )
                }
            ).flow.cachedIn(viewModelScope)
            
            val feedbacksPagingFlow = Pager(
                config = PagingConfig(
                    pageSize = 500,
                    enablePlaceholders = false
                ),
                pagingSourceFactory = {
                    FeedbackPagingSource(
                        getFeedbacksUseCase = getFeedbacksUseCase,
                        meetingId = meetingId,
                        pageSize = 500
                    )
                }
            ).flow.cachedIn(viewModelScope)
            
            _segmentsPagingFlow.value = segmentsPagingFlow
            _summariesPagingFlow.value = summariesPagingFlow
            _feedbacksPagingFlow.value = feedbacksPagingFlow

            var errorMessage: String? = null

            var startMillis: Long? = null
            var endMillis: Long? = null
            var actualStartTime: String = ""
            var actualEndTime: String = ""
            var actualDurationMinutes: Long = 0L

            when (val result = detailDeferred.await()) {
                is ApiResult.Success -> {
                    val meeting = result.data
                    val date = DateTimeUtils.localIsoToDateString(meeting.scheduledStartTime)
                    val location = meeting.location
                    if (meeting.actualStartTime != null && meeting.scheduledEndTime != null) {
                        Log.d(TAG, "[refreshAll] 시간 변환 시작")
                        Log.d(TAG, "  - 원본 actualStartTime: ${meeting.actualStartTime}")
                        Log.d(TAG, "  - 원본 scheduledEndTime: ${meeting.scheduledEndTime}")
                        
                        startMillis = DateTimeUtils.isoLocalDateTimeToMillis(meeting.actualStartTime)
                        endMillis = DateTimeUtils.isoLocalDateTimeToMillis(meeting.scheduledEndTime)
                        
                        Log.d(TAG, "  - 변환된 startMillis: $startMillis (${DateTimeUtils.millisToHourMinute(startMillis)})")
                        Log.d(TAG, "  - 변환된 endMillis: $endMillis (${DateTimeUtils.millisToHourMinute(endMillis)})")
                        
                        actualStartTime = DateTimeUtils.localIsoToTimeString(meeting.actualStartTime)
                        actualEndTime = DateTimeUtils.localIsoToTimeString(meeting.scheduledEndTime)
                        
                        actualDurationMinutes = DateTimeUtils.getDurationMinutes(startMillis, endMillis)!!
                        
                        Log.d(TAG, "  - 회의 지속 시간: ${actualDurationMinutes}분")
                        Log.d(TAG, "  - 시간 차이: ${(endMillis - startMillis) / 1000}초 (${(endMillis - startMillis) / 60000}분)")
                    } else {
                        Log.w(TAG, "[refreshAll] actualStartTime 또는 scheduledEndTime이 null입니다")
                        Log.w(TAG, "  - actualStartTime: ${meeting.actualStartTime}")
                        Log.w(TAG, "  - scheduledEndTime: ${meeting.scheduledEndTime}")
                    }

                    _state.update {
                        it.copy(
                            meetingTitle = meeting.title,
                            meetingDateAndLocation = "$date, $location",
                            actualStartTime = actualStartTime,
                            actualEndTime = actualEndTime,
                            actualDurationMinutes = actualDurationMinutes,
                            targetTime = meeting.targetTime,
                            agendas = meeting.agendas,
                            startMillis = startMillis
                        )
                    }
                    Log.d(TAG, "회의 상세 로드 성공: ${meeting}")

                    // participants의 email로 사용자 정보 미리 로드 (병렬 처리)
                    meeting.participants.forEach { participant ->
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

                is ApiResult.Failure -> {
                    Log.e(TAG, "회의 상세 로드 실패: ${result.message}")
                }
            }

            // 현재 사용자 ID 가져오기 (세그먼트 변환에 필요)
            var currentUserId: Long? = null
            try {
                when (val result = getMyProfileUseCase()) {
                    is ApiResult.Success -> {
                        currentUserId = result.data.id
                        _state.update { it.copy(currentUserId = currentUserId) }
                    }
                    is ApiResult.Failure -> {
                        Log.w(TAG, "현재 사용자 프로필 로드 실패: ${result.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "현재 사용자 프로필 로드 예외: ${e.message}", e)
            }

            when (val result = recapDeferred.await()) {
                is ApiResult.Success -> {
                    val recapItem = result.data.firstOrNull()
                    if (recapItem != null) {
                        val recap: String = recapItem.content
                        // generatedDateTime이 null이어도 recapSummary는 설정
                        _state.update { it.copy(recapSummary = recap) }
                        Log.d(TAG, "리캡 요약 로드 성공: ${recap}")
                    } else {
                        Log.d(TAG, "리캡 요약이 없음")
                        _state.update { it.copy(recapSummary = "") }
                    }
                }

                is ApiResult.Failure -> {
                    Log.e(TAG, "리캡 요약 로드 실패: ${result.message}")
                }
            }

            when (val result = participationDeferred.await()) {
                is ApiResult.Success -> {
                    val sorted = result.data.sortedByDescending { it.rate }
                    _state.update { it.copy(participationRates = sorted) }
                }

                is ApiResult.Failure -> Log.e(TAG, "참여율 로드 실패: ${result.message}")
            }


            when (val result = audioDeferred.await()) {
                is ApiResult.Success -> {
                    // 사용자 ID로 필터링하여 해당 사용자의 오디오 찾기
                    val userAudio = if (currentUserId != null) {
                        result.data.find { it.userId == currentUserId }
                    } else {
                        null
                    }
                    if (userAudio != null) {
                        _state.update { it.copy(audio = userAudio) }
                        Log.d(TAG, "오디오 로드 성공 (userId: $currentUserId): ${userAudio}")
                    } else {
                        if (currentUserId == null) {
                            Log.w(TAG, "현재 사용자 ID가 없어 오디오를 찾을 수 없습니다")
                        } else {
                            Log.d(TAG, "사용자 ID($currentUserId)에 해당하는 오디오가 없습니다")
                        }
                    }
                }

                is ApiResult.Failure -> {
                    Log.e(TAG, "오디오 목록 로드 실패: ${result.message}")
                }
            }

            _state.update { it.copy(isLoading = false, error = errorMessage) }
        }
    }
    
    // Segment를 SegmentUi로 변환하는 헬퍼 함수
    @RequiresApi(Build.VERSION_CODES.O)
    fun convertSegmentToUi(
        segment: com.imhungry.sillok.domain.model.segment.Segment,
        prevSegment: com.imhungry.sillok.domain.model.segment.Segment?,
        nextSegment: com.imhungry.sillok.domain.model.segment.Segment?,
        startMillis: Long?,
        currentUserId: Long?
    ): SegmentUi {
        val isSameAsPrevious = prevSegment != null && prevSegment.userId == segment.userId
        val isSameAsNext = nextSegment != null && nextSegment.userId == segment.userId

        Log.d(TAG, "[convertSegmentToUi] 세그먼트 변환 시작 (order: ${segment.segmentOrder})")
        Log.d(TAG, "  - 원본 timestamp: ${segment.timestamp}")
        Log.d(TAG, "  - startMillis: $startMillis")
        
        val millis = DateTimeUtils.isoLocalDateTimeToMillis(segment.timestamp)
        val elapsedString = DateTimeUtils.getElapsedStringFromMillis(startMillis, millis)
        
        Log.d(TAG, "  - 변환된 millis: $millis (${DateTimeUtils.millisToHourMinute(millis)})")
        Log.d(TAG, "  - 경과 시간 문자열: $elapsedString")
        if (startMillis != null && startMillis > 0) {
            Log.d(TAG, "  - 시작 시간으로부터 경과: ${(millis - startMillis) / 1000}초")
        }
        
        return SegmentUi(
            timestamp = elapsedString,
            text = segment.text,
            nickname = userNicknameCache[segment.userId] ?: "사용자 ${segment.userId}",
            profileImage = userProfileImageCache[segment.userId] ?: "",
            isFromCurrentUser = currentUserId != null && segment.userId == currentUserId,
            isSameAsPrevious = isSameAsPrevious,
            isSameAsNext = isSameAsNext,
            order = segment.segmentOrder,
            millis = millis
        )
    }

    // Feedback을 FeedbackUi로 변환하는 헬퍼 함수
    @RequiresApi(Build.VERSION_CODES.O)
    fun convertFeedbackToUi(
        feedback: com.imhungry.sillok.domain.model.feedback.Feedback,
        startMillis: Long?
    ): FeedbackUi {
        Log.d(TAG, "[convertFeedbackToUi] 피드백 변환 시작")
        Log.d(TAG, "  - 원본 generatedDateTime: ${feedback.generatedDateTime}")
        Log.d(TAG, "  - startMillis: $startMillis")
        
        val millis = DateTimeUtils.isoLocalDateTimeToMillis(feedback.generatedDateTime)
        val elapsedString = DateTimeUtils.getElapsedStringFromMillis(startMillis, millis)
        
        Log.d(TAG, "  - 변환된 millis: $millis (${DateTimeUtils.millisToHourMinute(millis)})")
        Log.d(TAG, "  - 경과 시간 문자열: $elapsedString")
        if (startMillis != null && startMillis > 0) {
            Log.d(TAG, "  - 시작 시간으로부터 경과: ${(millis - startMillis) / 1000}초")
        }
        
        return FeedbackUi(
            comment = feedback.comment,
            timestamp = elapsedString,
            isRead = false,
            millis = millis
        )
    }

    // Summary를 SummaryUi로 변환하는 헬퍼 함수
    @RequiresApi(Build.VERSION_CODES.O)
    fun convertSummaryToUi(
        summary: com.imhungry.sillok.domain.model.summary.Summary,
        startMillis: Long?
    ): SummaryUi {
        Log.d(TAG, "[convertSummaryToUi] 요약 변환 시작")
        Log.d(TAG, "  - 원본 generatedDateTime: ${summary.generatedDateTime}")
        Log.d(TAG, "  - startMillis: $startMillis")
        
        val millis = DateTimeUtils.isoLocalDateTimeToMillis(summary.generatedDateTime)
        val elapsedString = DateTimeUtils.getElapsedStringFromMillis(startMillis, millis)
        
        Log.d(TAG, "  - 변환된 millis: $millis (${DateTimeUtils.millisToHourMinute(millis)})")
        Log.d(TAG, "  - 경과 시간 문자열: $elapsedString")
        if (startMillis != null && startMillis > 0) {
            Log.d(TAG, "  - 시작 시간으로부터 경과: ${(millis - startMillis) / 1000}초")
        }
        
        return SummaryUi(
            content = summary.content,
            timestamp = elapsedString,
            millis = millis
        )
    }
}
