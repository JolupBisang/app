package com.imhungry.sillok.presentation.viewmodel.meetingminutes

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.imhungry.sillok.data.local.UserStore
import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.usecase.agenda.GetAgendasUseCase
import com.imhungry.sillok.domain.usecase.audio.GetAudioListUseCase
import com.imhungry.sillok.domain.usecase.feedback.GetFeedbacksUseCase
import com.imhungry.sillok.domain.usecase.meeting.GetMeetingDetailUseCase
import com.imhungry.sillok.domain.usecase.participation.GetParticipationRateHistoryUseCase
import com.imhungry.sillok.domain.usecase.segment.GetSegmentsUseCase
import com.imhungry.sillok.domain.usecase.summary.GetSummariesUseCase
import com.imhungry.sillok.presentation.state.meeting.FeedbackUi
import com.imhungry.sillok.presentation.state.meeting.SegmentUi
import com.imhungry.sillok.presentation.state.meeting.SummaryUi
import com.imhungry.sillok.presentation.state.meetingminutes.MeetingMinutesState
import com.imhungry.sillok.presentation.util.DateTimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class MeetingMinutesViewModel @Inject constructor(
    private val getMeetingDetailUseCase: GetMeetingDetailUseCase,
    private val getAgendasUseCase: GetAgendasUseCase,
    private val getSegmentsUseCase: GetSegmentsUseCase,
    private val getSummariesUseCase: GetSummariesUseCase,
    private val getParticipationRateHistoryUseCase: GetParticipationRateHistoryUseCase,
    private val getFeedbacksUseCase: GetFeedbacksUseCase,
    private val getAudioListUseCase: GetAudioListUseCase,
    private val userStore: UserStore
) : ViewModel() {
    companion object {
        private const val TAG = "MeetingMinutesViewModel"
    }

    private val _state = MutableStateFlow(MeetingMinutesState())
    val state: StateFlow<MeetingMinutesState> = _state.asStateFlow()

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
                    error = null,
                    // 페이징 상태 초기화
                    segmentsPage = 0,
                    summariesPage = 0,
                    feedbacksPage = 0,
                    hasMoreSegments = true,
                    hasMoreSummaries = true,
                    hasMoreFeedbacks = true
                )
            }

            val meetingId = state.value.meetingId
//
//            val detailDeferred = async { getMeetingDetailUseCase(meetingId) }
//            val agendasDeferred = async { getAgendasUseCase(meetingId) }
//            // 전체 데이터를 한 번에 로드 (충분히 큰 size 사용)
//            val segmentsDeferred = async { getSegmentsUseCase(meetingId, page = 0, size = 1000) }
//            val summariesDeferred = async { getSummariesUseCase(meetingId, page = 0, size = 500) }
//            val recapDeferred =
//                async { getSummariesUseCase(meetingId, isRecap = true, page = 0, size = 1) }
//            val participationDeferred = async { getParticipationRateHistoryUseCase(meetingId) }
//            // 전체 데이터를 한 번에 로드
//            val feedbacksDeferred = async { getFeedbacksUseCase(meetingId, page = 0, size = 500) }
            val audioDeferred = async { getAudioListUseCase(meetingId) }

            var errorMessage: String? = null
//
//            var startMillis: Long? = null
//            var endMillis: Long? = null
//
//            try {
//                val snapshot = FirebaseFirestore.getInstance()
//                    .collection("meetings")
//                    .document(meetingId.toString())
//                    .get()
//                    .await()
//                startMillis = snapshot.getLong("startMillis")
//            } catch (e: Exception) {
//                Log.e(TAG, "startMillis 조회 실패: ${e.message}", e)
//            }
//
//            try {
//                val snapshot = FirebaseFirestore.getInstance()
//                    .collection("meetings")
//                    .document(meetingId.toString())
//                    .get()
//                    .await()
//                endMillis = snapshot.getLong("endMillis")
//            } catch (e: Exception) {
//                Log.e(TAG, "endMillis 조회 실패: ${e.message}", e)
//            }
//
//            when (val result = detailDeferred.await()) {
//                is ApiResult.Success -> {
//                    val meeting = result.data
//                    val date = DateTimeUtils.localIsoToDateString(meeting.scheduledStartTime)
//                    val location = meeting.location
//
//                    _state.update {
//                        it.copy(
//                            meetingTitle = meeting.title,
//                            meetingDateAndLocation = "$date, $location",
//                            scheduledStartTime = DateTimeUtils.localIsoToTimeString(meeting.scheduledStartTime),
//                            targetTime = meeting.targetTime,
//                        )
//                    }
//                    Log.d(TAG, "회의 상세 로드 성공: ${meeting}")
//                }
//
//                is ApiResult.Failure -> {
//                    Log.e(TAG, "회의 상세 로드 실패: ${result.message}")
//                }
//            }
//
//            when (val result = agendasDeferred.await()) {
//                is ApiResult.Success -> _state.update { it.copy(agendas = result.data) }
//                is ApiResult.Failure -> Log.e(TAG, "아젠다 로드 실패: ${result.message}")
//            }
//
//            val currentUserId = userStore.user.first()?.id
//
//            when (val result = segmentsDeferred.await()) {
//                is ApiResult.Success -> {
//                    val ui = result.data.mapIndexed { index, seg ->
//                        val prevUserId = if (index > 0) result.data[index - 1].userId else null
//                        val nextUserId =
//                            if (index < result.data.lastIndex) result.data[index + 1].userId else null
//                        val isSameAsPrevious = prevUserId != null && prevUserId == seg.userId
//                        val isSameAsNext = nextUserId != null && nextUserId == seg.userId
//                        SegmentUi(
//                            timestamp = DateTimeUtils.getElapsedString(startMillis, seg.timestamp),
//                            text = seg.text,
//                            nickname = "사용자 ${seg.userId}",
//                            profileImage = "",
//                            isFromCurrentUser = currentUserId != null && seg.userId == currentUserId,
//                            isSameAsPrevious = isSameAsPrevious,
//                            isSameAsNext = isSameAsNext
//                        )
//                    }
//                    _state.update {
//                        it.copy(
//                            segments = ui,
//                            segmentsPage = 0,
//                            hasMoreSegments = false // 전체 로드이므로 더 이상 없음
//                        )
//                    }
//                }
//
//                is ApiResult.Failure -> Log.e(TAG, "세그먼트 로드 실패: ${result.message}")
//            }
//
//            when (val result = summariesDeferred.await()) {
//                is ApiResult.Success -> {
//                    val ui = result.data.map {
//                        SummaryUi(
//                            content = it.content,
//                            timestamp = DateTimeUtils.getElapsedString(
//                                startMillis,
//                                it.generatedDateTime
//                            )
//                        )
//                    }
//                    _state.update {
//                        it.copy(
//                            summaries = ui,
//                            summariesPage = 0,
//                            hasMoreSummaries = false // 전체 로드이므로 더 이상 없음
//                        )
//                    }
//                }
//
//                is ApiResult.Failure -> Log.e(TAG, "요약 로드 실패: ${result.message}")
//            }
//
//            when (val result = recapDeferred.await()) {
//                is ApiResult.Success -> {
//                    val recap: String = result.data.first().content
//                    // generatedDateTime이 null이어도 recapSummary는 설정
//                    _state.update { it.copy(recapSummary = recap) }
//                    Log.d(TAG, "리캡 요약 로드 성공: ${recap}")
//                }
//
//                is ApiResult.Failure -> {
//                    Log.e(TAG, "리캡 요약 로드 실패: ${result.message}")
//                }
//            }
//
//            when (val result = participationDeferred.await()) {
//                is ApiResult.Success -> {
//                    val sorted = result.data.sortedByDescending { it.rate }
//                    _state.update { it.copy(participationRates = sorted) }
//                }
//
//                is ApiResult.Failure -> Log.e(TAG, "참여율 로드 실패: ${result.message}")
//            }
//
//            when (val result = feedbacksDeferred.await()) {
//                is ApiResult.Success -> {
//                    val ui = result.data.map {
//                        FeedbackUi(
//                            comment = it.comment,
//                            timestamp = DateTimeUtils.getElapsedString(
//                                startMillis,
//                                it.generatedDateTime
//                            ),
//                            isRead = false
//                        )
//                    }
//                    _state.update {
//                        it.copy(
//                            feedbacks = ui,
//                            feedbacksPage = 0,
//                            hasMoreFeedbacks = false // 전체 로드이므로 더 이상 없음
//                        )
//                    }
//                }
//
//                is ApiResult.Failure -> Log.e(TAG, "피드백 로드 실패: ${result.message}")
//            }

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

    /**
     * 세그먼트 더 불러오기 (무한 스크롤)
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun loadMoreSegments() {
        val currentState = state.value
        if (!currentState.hasMoreSegments || currentState.isLoadingMoreSegments) {
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoadingMoreSegments = true) }

            val meetingId = currentState.meetingId
            val nextPage = currentState.segmentsPage + 1

            // Firebase에서 startMillis 조회
            var startMillis: Long? = null
            try {
                val snapshot = FirebaseFirestore.getInstance()
                    .collection("meetings")
                    .document(meetingId.toString())
                    .get()
                    .await()
                startMillis = snapshot.getLong("startMillis")
            } catch (e: Exception) {
                Log.e(TAG, "startMillis 조회 실패: ${e.message}", e)
            }

            when (val result = getSegmentsUseCase(meetingId, page = nextPage, size = 40)) {
                is ApiResult.Success -> {
                    val currentUserId = userStore.user.first()?.id
                    val existingSegments = currentState.segments

                    // 기존 세그먼트의 마지막 userId 확인 (isSameAsNext 업데이트용)
                    val lastSegment = existingSegments.lastOrNull()

                    val newUi = result.data.mapIndexed { index, seg ->
                        val prevUserId = if (index > 0) {
                            result.data[index - 1].userId
                        } else {
                            // 첫 번째 새 세그먼트인 경우, 기존 마지막 세그먼트의 userId 추출
                            lastSegment?.nickname?.removePrefix("사용자 ")?.toLongOrNull()
                        }
                        val nextUserId =
                            if (index < result.data.lastIndex) result.data[index + 1].userId else null
                        val isSameAsPrevious = prevUserId != null && prevUserId == seg.userId
                        val isSameAsNext = nextUserId != null && nextUserId == seg.userId

                        SegmentUi(
                            timestamp = DateTimeUtils.getElapsedString(startMillis, seg.timestamp),
                            text = seg.text,
                            nickname = "사용자 ${seg.userId}",
                            profileImage = "",
                            isFromCurrentUser = currentUserId != null && seg.userId == currentUserId,
                            isSameAsPrevious = isSameAsPrevious,
                            isSameAsNext = isSameAsNext
                        )
                    }

                    // 기존 마지막 세그먼트의 isSameAsNext 업데이트
                    val updatedExistingSegments =
                        if (existingSegments.isNotEmpty() && newUi.isNotEmpty()) {
                            val lastIndex = existingSegments.lastIndex
                            val lastExisting = existingSegments[lastIndex]
                            val firstNew = newUi[0]
                            if (lastExisting.nickname == firstNew.nickname) {
                                existingSegments.toMutableList().apply {
                                    this[lastIndex] = lastExisting.copy(isSameAsNext = true)
                                }
                            } else {
                                existingSegments
                            }
                        } else {
                            existingSegments
                        }

                    _state.update {
                        it.copy(
                            segments = updatedExistingSegments + newUi,
                            segmentsPage = nextPage,
                            hasMoreSegments = result.data.size >= 40,
                            isLoadingMoreSegments = false
                        )
                    }
                }

                is ApiResult.Failure -> {
                    Log.e(TAG, "세그먼트 더 불러오기 실패: ${result.message}")
                    _state.update { it.copy(isLoadingMoreSegments = false) }
                }
            }
        }
    }

    /**
     * 요약 더 불러오기 (무한 스크롤)
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun loadMoreSummaries() {
        val currentState = state.value
        if (!currentState.hasMoreSummaries || currentState.isLoadingMoreSummaries) {
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoadingMoreSummaries = true) }

            val meetingId = currentState.meetingId
            val nextPage = currentState.summariesPage + 1

            // Firebase에서 startMillis 조회
            var startMillis: Long? = null
            try {
                val snapshot = FirebaseFirestore.getInstance()
                    .collection("meetings")
                    .document(meetingId.toString())
                    .get()
                    .await()
                startMillis = snapshot.getLong("startMillis")
            } catch (e: Exception) {
                Log.e(TAG, "startMillis 조회 실패: ${e.message}", e)
            }

            when (val result = getSummariesUseCase(meetingId, page = nextPage, size = 30)) {
                is ApiResult.Success -> {
                    val newUi = result.data.map {
                        SummaryUi(
                            content = it.content,
                            timestamp = DateTimeUtils.getElapsedString(
                                startMillis,
                                it.generatedDateTime
                            )
                        )
                    }

                    _state.update {
                        it.copy(
                            summaries = it.summaries + newUi,
                            summariesPage = nextPage,
                            hasMoreSummaries = result.data.size >= 30,
                            isLoadingMoreSummaries = false
                        )
                    }
                }

                is ApiResult.Failure -> {
                    Log.e(TAG, "요약 더 불러오기 실패: ${result.message}")
                    _state.update { it.copy(isLoadingMoreSummaries = false) }
                }
            }
        }
    }

    /**
     * 피드백 더 불러오기 (무한 스크롤)
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun loadMoreFeedbacks() {
        val currentState = state.value
        if (!currentState.hasMoreFeedbacks || currentState.isLoadingMoreFeedbacks) {
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoadingMoreFeedbacks = true) }

            val meetingId = currentState.meetingId
            val nextPage = currentState.feedbacksPage + 1

            // Firebase에서 startMillis 조회
            var startMillis: Long? = null
            try {
                val snapshot = FirebaseFirestore.getInstance()
                    .collection("meetings")
                    .document(meetingId.toString())
                    .get()
                    .await()
                startMillis = snapshot.getLong("startMillis")
            } catch (e: Exception) {
                Log.e(TAG, "startMillis 조회 실패: ${e.message}", e)
            }

            when (val result = getFeedbacksUseCase(meetingId, page = nextPage, size = 30)) {
                is ApiResult.Success -> {
                    val newUi = result.data.map {
                        FeedbackUi(
                            comment = it.comment,
                            timestamp = DateTimeUtils.getElapsedString(
                                startMillis,
                                it.generatedDateTime
                            ),
                            isRead = false
                        )
                    }

                    _state.update {
                        it.copy(
                            feedbacks = it.feedbacks + newUi,
                            feedbacksPage = nextPage,
                            hasMoreFeedbacks = result.data.size >= 30,
                            isLoadingMoreFeedbacks = false
                        )
                    }
                }

                is ApiResult.Failure -> {
                    Log.e(TAG, "피드백 더 불러오기 실패: ${result.message}")
                    _state.update { it.copy(isLoadingMoreFeedbacks = false) }
                }
            }
        }
    }

    /**
     * 더미 데이터를 사용하여 회의록 화면의 상태를 채웁니다.
     */
    fun loadDummyMeetingMinutesState() {
        val dummyAgendas = listOf(
            com.imhungry.sillok.domain.model.agenda.Agenda(
                agendaId = 1L,
                content = "프로젝트 목표 합의",
                isCompleted = true
            ),
            com.imhungry.sillok.domain.model.agenda.Agenda(
                agendaId = 2L,
                content = "역할 및 일정 수립",
                isCompleted = false
            ),
            com.imhungry.sillok.domain.model.agenda.Agenda(
                agendaId = 3L,
                content = "리스크 식별",
                isCompleted = false
            ),
        )

        val dummyParticipation = listOf(
            com.imhungry.sillok.domain.model.participation.UserParticipationRate(
                userId = 1L,
                rate = 0.32,
                totalParticipationChunk = 100L
            ),
            com.imhungry.sillok.domain.model.participation.UserParticipationRate(
                userId = 2L,
                rate = 0.27,
                totalParticipationChunk = 85L
            ),
            com.imhungry.sillok.domain.model.participation.UserParticipationRate(
                userId = 3L,
                rate = 0.18,
                totalParticipationChunk = 55L
            ),
        )

        val dummySegments = listOf(
            SegmentUi(
                timestamp = "00:01:10",
                text = "안녕하세요, 오늘 아젠다는...",
                nickname = "홍길동",
                profileImage = "",
                isFromCurrentUser = false,
                isSameAsPrevious = false,
                isSameAsNext = true
            ),
            SegmentUi(
                timestamp = "00:01:35",
                text = "첫 번째로 목표 범위를 정하면...",
                nickname = "홍길동",
                profileImage = "",
                isFromCurrentUser = false,
                isSameAsPrevious = true,
                isSameAsNext = false
            ),
            SegmentUi(
                timestamp = "00:02:10",
                text = "디자인 관점에서 보면...",
                nickname = "김디자",
                profileImage = "",
                isFromCurrentUser = false,
                isSameAsPrevious = false,
                isSameAsNext = false
            ),
            SegmentUi(
                timestamp = "00:03:45",
                text = "백엔드 API는...",
                nickname = "이개발",
                profileImage = "",
                isFromCurrentUser = true,
                isSameAsPrevious = false,
                isSameAsNext = false
            ),
            SegmentUi(
                timestamp = "00:01:10",
                text = "안녕하세요, 오늘 아젠다는...",
                nickname = "홍길동",
                profileImage = "",
                isFromCurrentUser = false,
                isSameAsPrevious = false,
                isSameAsNext = true
            ),
            SegmentUi(
                timestamp = "00:01:35",
                text = "첫 번째로 목표 범위를 정하면...",
                nickname = "홍길동",
                profileImage = "",
                isFromCurrentUser = false,
                isSameAsPrevious = true,
                isSameAsNext = false
            ),
            SegmentUi(
                timestamp = "00:02:10",
                text = "디자인 관점에서 보면...",
                nickname = "김디자",
                profileImage = "",
                isFromCurrentUser = false,
                isSameAsPrevious = false,
                isSameAsNext = false
            ),
            SegmentUi(
                timestamp = "00:03:45",
                text = "백엔드 API는...",
                nickname = "이개발",
                profileImage = "",
                isFromCurrentUser = true,
                isSameAsPrevious = false,
                isSameAsNext = false
            ),
            SegmentUi(
                timestamp = "00:01:10",
                text = "안녕하세요, 오늘 아젠다는...",
                nickname = "홍길동",
                profileImage = "",
                isFromCurrentUser = false,
                isSameAsPrevious = false,
                isSameAsNext = true
            ),
            SegmentUi(
                timestamp = "00:01:35",
                text = "첫 번째로 목표 범위를 정하면...",
                nickname = "홍길동",
                profileImage = "",
                isFromCurrentUser = false,
                isSameAsPrevious = true,
                isSameAsNext = false
            ),
            SegmentUi(
                timestamp = "00:02:10",
                text = "디자인 관점에서 보면...",
                nickname = "김디자",
                profileImage = "",
                isFromCurrentUser = false,
                isSameAsPrevious = false,
                isSameAsNext = false
            ),
            SegmentUi(
                timestamp = "00:03:45",
                text = "백엔드 API는...",
                nickname = "이개발",
                profileImage = "",
                isFromCurrentUser = true,
                isSameAsPrevious = false,
                isSameAsNext = false
            ),
            SegmentUi(
                timestamp = "00:01:10",
                text = "안녕하세요, 오늘 아젠다는...",
                nickname = "홍길동",
                profileImage = "",
                isFromCurrentUser = false,
                isSameAsPrevious = false,
                isSameAsNext = true
            ),
            SegmentUi(
                timestamp = "00:01:35",
                text = "첫 번째로 목표 범위를 정하면...",
                nickname = "홍길동",
                profileImage = "",
                isFromCurrentUser = false,
                isSameAsPrevious = true,
                isSameAsNext = false
            ),
            SegmentUi(
                timestamp = "00:02:10",
                text = "디자인 관점에서 보면...",
                nickname = "김디자",
                profileImage = "",
                isFromCurrentUser = false,
                isSameAsPrevious = false,
                isSameAsNext = false
            ),
            SegmentUi(
                timestamp = "00:03:45",
                text = "백엔드 API는...",
                nickname = "이개발",
                profileImage = "",
                isFromCurrentUser = true,
                isSameAsPrevious = false,
                isSameAsNext = false
            ),
            SegmentUi(
                timestamp = "00:01:10",
                text = "안녕하세요, 오늘 아젠다는...",
                nickname = "홍길동",
                profileImage = "",
                isFromCurrentUser = false,
                isSameAsPrevious = false,
                isSameAsNext = true
            ),
            SegmentUi(
                timestamp = "00:01:35",
                text = "첫 번째로 목표 범위를 정하면...",
                nickname = "홍길동",
                profileImage = "",
                isFromCurrentUser = false,
                isSameAsPrevious = true,
                isSameAsNext = false
            ),
            SegmentUi(
                timestamp = "00:02:10",
                text = "디자인 관점에서 보면...",
                nickname = "김디자",
                profileImage = "",
                isFromCurrentUser = false,
                isSameAsPrevious = false,
                isSameAsNext = false
            ),
            SegmentUi(
                timestamp = "00:03:45",
                text = "백엔드 API는...",
                nickname = "이개발",
                profileImage = "",
                isFromCurrentUser = true,
                isSameAsPrevious = false,
                isSameAsNext = false
            ),
            SegmentUi(
                timestamp = "00:01:10",
                text = "안녕하세요, 오늘 아젠다는...",
                nickname = "홍길동",
                profileImage = "",
                isFromCurrentUser = false,
                isSameAsPrevious = false,
                isSameAsNext = true
            ),
            SegmentUi(
                timestamp = "00:01:35",
                text = "첫 번째로 목표 범위를 정하면...",
                nickname = "홍길동",
                profileImage = "",
                isFromCurrentUser = false,
                isSameAsPrevious = true,
                isSameAsNext = false
            ),
            SegmentUi(
                timestamp = "00:02:10",
                text = "디자인 관점에서 보면...",
                nickname = "김디자",
                profileImage = "",
                isFromCurrentUser = false,
                isSameAsPrevious = false,
                isSameAsNext = false
            ),
            SegmentUi(
                timestamp = "00:03:45",
                text = "백엔드 API는...",
                nickname = "이개발",
                profileImage = "",
                isFromCurrentUser = true,
                isSameAsPrevious = false,
                isSameAsNext = false
            ),
            SegmentUi(
                timestamp = "00:01:10",
                text = "안녕하세요, 오늘 아젠다는...",
                nickname = "홍길동",
                profileImage = "",
                isFromCurrentUser = false,
                isSameAsPrevious = false,
                isSameAsNext = true
            ),
            SegmentUi(
                timestamp = "00:01:35",
                text = "첫 번째로 목표 범위를 정하면...",
                nickname = "홍길동",
                profileImage = "",
                isFromCurrentUser = false,
                isSameAsPrevious = true,
                isSameAsNext = false
            ),
            SegmentUi(
                timestamp = "00:02:10",
                text = "디자인 관점에서 보면...",
                nickname = "김디자",
                profileImage = "",
                isFromCurrentUser = false,
                isSameAsPrevious = false,
                isSameAsNext = false
            ),
            SegmentUi(
                timestamp = "00:03:45",
                text = "백엔드 API는...",
                nickname = "이개발",
                profileImage = "",
                isFromCurrentUser = true,
                isSameAsPrevious = false,
                isSameAsNext = false
            )
        )

        val dummySummaries = listOf(
            com.imhungry.sillok.presentation.state.meeting.SummaryUi(
                content = "프로젝트 목표와 범위 합의 완료.",
                timestamp = "00:05:00"
            ),
            com.imhungry.sillok.presentation.state.meeting.SummaryUi(
                content = "1차 마일스톤: 로그인/회원/기본 목록.",
                timestamp = "00:12:40"
            ),
            com.imhungry.sillok.presentation.state.meeting.SummaryUi(
                content = "프로젝트 목표와 범위 합의 완료.",
                timestamp = "00:05:00"
            ),
            com.imhungry.sillok.presentation.state.meeting.SummaryUi(
                content = "1차 마일스톤: 로그인/회원/기본 목록.",
                timestamp = "00:12:40"
            ),
            com.imhungry.sillok.presentation.state.meeting.SummaryUi(
                content = "프로젝트 목표와 범위 합의 완료.",
                timestamp = "00:05:00"
            ),
            com.imhungry.sillok.presentation.state.meeting.SummaryUi(
                content = "1차 마일스톤: 로그인/회원/기본 목록.",
                timestamp = "00:12:40"
            ),
            com.imhungry.sillok.presentation.state.meeting.SummaryUi(
                content = "프로젝트 목표와 범위 합의 완료.",
                timestamp = "00:05:00"
            ),
            com.imhungry.sillok.presentation.state.meeting.SummaryUi(
                content = "1차 마일스톤: 로그인/회원/기본 목록.",
                timestamp = "00:12:40"
            ),
            com.imhungry.sillok.presentation.state.meeting.SummaryUi(
                content = "프로젝트 목표와 범위 합의 완료.",
                timestamp = "00:05:00"
            ),
            com.imhungry.sillok.presentation.state.meeting.SummaryUi(
                content = "1차 마일스톤: 로그인/회원/기본 목록.",
                timestamp = "00:12:40"
            )
        )

        val dummyFeedbacks = listOf(
            com.imhungry.sillok.presentation.state.meeting.FeedbackUi(
                comment = "회의 종료까지 10분 남았습니다.",
                timestamp = "01:20:00",
                isRead = true
            ),
            com.imhungry.sillok.presentation.state.meeting.FeedbackUi(
                comment = "다음 안건으로 넘어가 주세요.",
                timestamp = "00:25:30",
                isRead = true
            ),
            com.imhungry.sillok.presentation.state.meeting.FeedbackUi(
                comment = "회의 종료까지 10분 남았습니다.",
                timestamp = "01:20:00",
                isRead = true
            ),
            com.imhungry.sillok.presentation.state.meeting.FeedbackUi(
                comment = "다음 안건으로 넘어가 주세요.",
                timestamp = "00:25:30",
                isRead = true
            ),
            com.imhungry.sillok.presentation.state.meeting.FeedbackUi(
                comment = "회의 종료까지 10분 남았습니다.",
                timestamp = "01:20:00",
                isRead = true
            ),
            com.imhungry.sillok.presentation.state.meeting.FeedbackUi(
                comment = "다음 안건으로 넘어가 주세요.",
                timestamp = "00:25:30",
                isRead = true
            ),
            com.imhungry.sillok.presentation.state.meeting.FeedbackUi(
                comment = "회의 종료까지 10분 남았습니다.",
                timestamp = "01:20:00",
                isRead = true
            ),
            com.imhungry.sillok.presentation.state.meeting.FeedbackUi(
                comment = "다음 안건으로 넘어가 주세요.",
                timestamp = "00:25:30",
                isRead = true
            ),
            com.imhungry.sillok.presentation.state.meeting.FeedbackUi(
                comment = "회의 종료까지 10분 남았습니다.",
                timestamp = "01:20:00",
                isRead = true
            ),
            com.imhungry.sillok.presentation.state.meeting.FeedbackUi(
                comment = "다음 안건으로 넘어가 주세요.",
                timestamp = "00:25:30",
                isRead = true
            )
        )

        _state.update { current ->
            current.copy(
                meetingTitle = "프로젝트 킥오프 회의",
                meetingDateAndLocation = "2025.03.26 수, IT관 777호",
                agendas = dummyAgendas,
                feedbacks = dummyFeedbacks,
                scheduledStartTime = "14:00",
                scheduledEndTime = "15:30",
                targetTime = 90,
                actualStartTime = "14:05",
                actualEndTime = "15:28",
                actualDurationMinutes = 83,
                participationRates = dummyParticipation.sortedByDescending { it.rate },
                segments = dummySegments,
                summaries = dummySummaries,
                recapSummary = "목표/범위 합의, 1차 마일스톤 정의",
                isLoading = false,
                error = null
            )
        }
    }
}
