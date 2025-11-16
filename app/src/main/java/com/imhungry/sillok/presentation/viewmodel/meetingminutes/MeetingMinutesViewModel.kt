package com.imhungry.sillok.presentation.viewmodel.meetingminutes

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.model.audio.AudioInfo
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
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.imhungry.sillok.data.paging.SegmentPagingSource
import com.imhungry.sillok.data.paging.SummaryPagingSource
import com.imhungry.sillok.data.paging.FeedbackPagingSource
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
                        startMillis = DateTimeUtils.isoLocalDateTimeToMillis(meeting.actualStartTime)
                        endMillis = DateTimeUtils.isoLocalDateTimeToMillis(meeting.scheduledEndTime)
                        actualStartTime = DateTimeUtils.localIsoToTimeString(meeting.actualStartTime)
                        actualEndTime = DateTimeUtils.localIsoToTimeString(meeting.scheduledEndTime)
                        actualDurationMinutes = DateTimeUtils.getDurationMinutes(startMillis, endMillis)!!
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
                    val first = result.data.firstOrNull()
                    if (first != null) {
                        _state.update { it.copy(audio = first) }
                        Log.d(TAG, "오디오 로드 성공: ${first}")
                    } else {
                        Log.d(TAG, "오디오 목록이 비어 있습니다")
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

        val millis = DateTimeUtils.isoLocalDateTimeToMillis(segment.timestamp)
        return SegmentUi(
            timestamp = DateTimeUtils.getElapsedStringFromMillis(startMillis, millis),
            text = segment.text,
            nickname = userNicknameCache[segment.userId] ?: "사용자 ${segment.userId}",
            profileImage = userProfileImageCache[segment.userId] ?: "",
            isFromCurrentUser = currentUserId != null && segment.userId == currentUserId,
            isSameAsPrevious = isSameAsPrevious,
            isSameAsNext = isSameAsNext,
            order = segment.segmentOrder
        )
    }
}
