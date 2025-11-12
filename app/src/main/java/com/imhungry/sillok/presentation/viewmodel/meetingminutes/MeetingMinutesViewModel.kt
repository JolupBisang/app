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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
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

            val detailDeferred = async { getMeetingDetailUseCase(meetingId) }
            // 전체 데이터를 한 번에 로드 (충분히 큰 size 사용)
            val segmentsDeferred = async { getSegmentsUseCase(meetingId, page = 0, size = 1000) }
            val summariesDeferred = async { getSummariesUseCase(meetingId, page = 0, size = 500) }
            val recapDeferred =
                async { getSummariesUseCase(meetingId, isRecap = true, page = 0, size = 1) }
            val participationDeferred = async { getParticipationRateHistoryUseCase(meetingId) }
            // 전체 데이터를 한 번에 로드
            val feedbacksDeferred = async { getFeedbacksUseCase(meetingId, page = 0, size = 500) }
            val audioDeferred = async { getAudioListUseCase(meetingId) }

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
                            agendas = meeting.agendas
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

            // 현재 사용자 ID 가져오기
            var currentUserId: Long? = null
            try {
                when (val result = getMyProfileUseCase()) {
                    is ApiResult.Success -> {
                        currentUserId = result.data.id
                    }
                    is ApiResult.Failure -> {
                        Log.w(TAG, "현재 사용자 프로필 로드 실패: ${result.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "현재 사용자 프로필 로드 예외: ${e.message}", e)
            }

            when (val result = segmentsDeferred.await()) {
                is ApiResult.Success -> {
                    val ui = result.data.mapIndexed { index, seg ->
                        val prevUserId = if (index > 0) result.data[index - 1].userId else null
                        val nextUserId =
                            if (index < result.data.lastIndex) result.data[index + 1].userId else null
                        val isSameAsPrevious = prevUserId != null && prevUserId == seg.userId
                        val isSameAsNext = nextUserId != null && nextUserId == seg.userId

                        val millis = DateTimeUtils.isoLocalDateTimeToMillis(seg.timestamp)
                        SegmentUi(
                            timestamp = DateTimeUtils.getElapsedStringFromMillis(startMillis, millis),
                            text = seg.text,
                            nickname = userNicknameCache[seg.userId] ?: "사용자 ${seg.userId}",
                            profileImage = userProfileImageCache[seg.userId] ?: "",
                            isFromCurrentUser = currentUserId != null && seg.userId == currentUserId,
                            isSameAsPrevious = isSameAsPrevious,
                            isSameAsNext = isSameAsNext
                        )
                    }
                    _state.update {
                        it.copy(
                            segments = ui,
                            segmentsPage = 0,
                            hasMoreSegments = false // 전체 로드이므로 더 이상 없음
                        )
                    }
                }

                is ApiResult.Failure -> Log.e(TAG, "세그먼트 로드 실패: ${result.message}")
            }

            when (val result = summariesDeferred.await()) {
                is ApiResult.Success -> {
                    val ui = result.data.map {
                        val millis = DateTimeUtils.isoLocalDateTimeToMillis(it.generatedDateTime)
                        SummaryUi(
                            content = it.content,
                            timestamp = DateTimeUtils.getElapsedStringFromMillis(
                                startMillis,
                                millis
                            )
                        )
                    }
                    _state.update {
                        it.copy(
                            summaries = ui,
                            summariesPage = 0,
                            hasMoreSummaries = false // 전체 로드이므로 더 이상 없음
                        )
                    }
                }

                is ApiResult.Failure -> Log.e(TAG, "요약 로드 실패: ${result.message}")
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

            when (val result = feedbacksDeferred.await()) {
                is ApiResult.Success -> {
                    val ui = result.data.map {
                        val millis = DateTimeUtils.isoLocalDateTimeToMillis(it.generatedDateTime)
                        FeedbackUi(
                            comment = it.comment,
                            timestamp = DateTimeUtils.getElapsedStringFromMillis(
                                startMillis,
                                millis
                            ),
                            isRead = false
                        )
                    }
                    _state.update {
                        it.copy(
                            feedbacks = ui,
                            feedbacksPage = 0,
                            hasMoreFeedbacks = false // 전체 로드이므로 더 이상 없음
                        )
                    }
                }

                is ApiResult.Failure -> Log.e(TAG, "피드백 로드 실패: ${result.message}")
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

    /**
     * 더미 데이터를 사용하여 회의록 화면의 상태를 채웁니다.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun loadDummyMeetingMinutesState() {
        val start = DateTimeUtils.utcToKoreaTime(DateTimeUtils.getCurrentUtcTime())
        val end = DateTimeUtils.utcToKoreaTime(DateTimeUtils.getCurrentUtcTimePlusOneHour())
        val startMillis = DateTimeUtils.isoLocalDateTimeToMillis(start)
        val endMillis = DateTimeUtils.isoLocalDateTimeToMillis(end)
        val actualStartTime = DateTimeUtils.localIsoToTimeString(start)
        val actualEndTime = DateTimeUtils.localIsoToTimeString(end)
        val actualDurationMinutes = DateTimeUtils.getDurationMinutes(startMillis, endMillis)!!

        viewModelScope.launch {
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
                nickname = "",
                rate = 0.32,
            ),
            com.imhungry.sillok.domain.model.participation.UserParticipationRate(
                userId = 2L,
                nickname = "",
                rate = 0.27,
            ),
            com.imhungry.sillok.domain.model.participation.UserParticipationRate(
                userId = 3L,
                nickname = "",
                rate = 0.18,
            ),
        )

        val dummySegments = listOf(
            SegmentUi(
                timestamp = "00:00:10",
                text = "안녕하세요, 오늘 아젠다는...",
                nickname = "홍길동",
                profileImage = "",
                isFromCurrentUser = false,
                isSameAsPrevious = false,
                isSameAsNext = true
            ),
            SegmentUi(
                timestamp = "00:00:30",
                text = "첫 번째로 목표 범위를 정하면...",
                nickname = "홍길동",
                profileImage = "",
                isFromCurrentUser = false,
                isSameAsPrevious = true,
                isSameAsNext = false
            ),
            SegmentUi(
                timestamp = "00:00:50",
                text = "디자인 관점에서 보면...",
                nickname = "김디자",
                profileImage = "",
                isFromCurrentUser = false,
                isSameAsPrevious = false,
                isSameAsNext = false
            ),
            SegmentUi(
                timestamp = "00:01:00",
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
                timestamp = "00:01:50",
                text = "디자인 관점에서 보면...",
                nickname = "김디자",
                profileImage = "",
                isFromCurrentUser = false,
                isSameAsPrevious = false,
                isSameAsNext = false
            ),
            SegmentUi(
                timestamp = "00:02:00",
                text = "백엔드 API는...",
                nickname = "이개발",
                profileImage = "",
                isFromCurrentUser = true,
                isSameAsPrevious = false,
                isSameAsNext = false
            ),
            SegmentUi(
                timestamp = "00:02:10",
                text = "안녕하세요, 오늘 아젠다는...",
                nickname = "홍길동",
                profileImage = "",
                isFromCurrentUser = false,
                isSameAsPrevious = false,
                isSameAsNext = true
            ),
            SegmentUi(
                timestamp = "00:02:15",
                text = "첫 번째로 목표 범위를 정하면...",
                nickname = "홍길동",
                profileImage = "",
                isFromCurrentUser = false,
                isSameAsPrevious = true,
                isSameAsNext = false
            ),
            SegmentUi(
                timestamp = "00:02:20",
                text = "디자인 관점에서 보면...",
                nickname = "김디자",
                profileImage = "",
                isFromCurrentUser = false,
                isSameAsPrevious = false,
                isSameAsNext = false
            ),
            SegmentUi(
                timestamp = "00:02:22",
                text = "백엔드 API는...",
                nickname = "이개발",
                profileImage = "",
                isFromCurrentUser = true,
                isSameAsPrevious = false,
                isSameAsNext = false
            ),
            SegmentUi(
                timestamp = "00:02:30",
                text = "안녕하세요, 오늘 아젠다는...",
                nickname = "홍길동",
                profileImage = "",
                isFromCurrentUser = false,
                isSameAsPrevious = false,
                isSameAsNext = true
            ),
            SegmentUi(
                timestamp = "00:02:42",
                text = "첫 번째로 목표 범위를 정하면...",
                nickname = "홍길동",
                profileImage = "",
                isFromCurrentUser = false,
                isSameAsPrevious = true,
                isSameAsNext = false
            ),
            SegmentUi(
                timestamp = "00:02:55",
                text = "디자인 관점에서 보면...",
                nickname = "김디자",
                profileImage = "",
                isFromCurrentUser = false,
                isSameAsPrevious = false,
                isSameAsNext = false
            ),
            SegmentUi(
                timestamp = "00:03:00",
                text = "백엔드 API는...",
                nickname = "이개발",
                profileImage = "",
                isFromCurrentUser = true,
                isSameAsPrevious = false,
                isSameAsNext = false
            ),
            SegmentUi(
                timestamp = "00:03:10",
                text = "안녕하세요, 오늘 아젠다는...",
                nickname = "홍길동",
                profileImage = "",
                isFromCurrentUser = false,
                isSameAsPrevious = false,
                isSameAsNext = true
            ),
            SegmentUi(
                timestamp = "00:03:13",
                text = "첫 번째로 목표 범위를 정하면...",
                nickname = "홍길동",
                profileImage = "",
                isFromCurrentUser = false,
                isSameAsPrevious = true,
                isSameAsNext = false
            ),
            SegmentUi(
                timestamp = "00:03:23",
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
                timestamp = "00:03:55",
                text = "안녕하세요, 오늘 아젠다는...",
                nickname = "홍길동",
                profileImage = "",
                isFromCurrentUser = false,
                isSameAsPrevious = false,
                isSameAsNext = true
            ),
            SegmentUi(
                timestamp = "00:03:59",
                text = "첫 번째로 목표 범위를 정하면...",
                nickname = "홍길동",
                profileImage = "",
                isFromCurrentUser = false,
                isSameAsPrevious = true,
                isSameAsNext = false
            ),
            SegmentUi(
                timestamp = "00:04:10",
                text = "디자인 관점에서 보면...",
                nickname = "김디자",
                profileImage = "",
                isFromCurrentUser = false,
                isSameAsPrevious = false,
                isSameAsNext = false
            ),
            SegmentUi(
                timestamp = "00:04:20",
                text = "백엔드 API는...",
                nickname = "이개발",
                profileImage = "",
                isFromCurrentUser = true,
                isSameAsPrevious = false,
                isSameAsNext = false
            ),
            SegmentUi(
                timestamp = "00:04:22",
                text = "안녕하세요, 오늘 아젠다는...",
                nickname = "홍길동",
                profileImage = "",
                isFromCurrentUser = false,
                isSameAsPrevious = false,
                isSameAsNext = true
            ),
            SegmentUi(
                timestamp = "00:04:30",
                text = "첫 번째로 목표 범위를 정하면...",
                nickname = "홍길동",
                profileImage = "",
                isFromCurrentUser = false,
                isSameAsPrevious = true,
                isSameAsNext = false
            ),
            SegmentUi(
                timestamp = "00:04:34",
                text = "디자인 관점에서 보면...",
                nickname = "김디자",
                profileImage = "",
                isFromCurrentUser = false,
                isSameAsPrevious = false,
                isSameAsNext = false
            ),
            SegmentUi(
                timestamp = "00:04:37",
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
                timestamp = "00:00:03"
            ),
            com.imhungry.sillok.presentation.state.meeting.SummaryUi(
                content = "1차 마일스톤: 로그인/회원/기본 목록.",
                timestamp = "00:00:07"
            ),
            com.imhungry.sillok.presentation.state.meeting.SummaryUi(
                content = "프로젝트 목표와 범위 합의 완료.",
                timestamp = "00:00:12"
            ),
            com.imhungry.sillok.presentation.state.meeting.SummaryUi(
                content = "1차 마일스톤: 로그인/회원/기본 목록.",
                timestamp = "00:00:15"
            ),
            com.imhungry.sillok.presentation.state.meeting.SummaryUi(
                content = "프로젝트 목표와 범위 합의 완료.",
                timestamp = "00:00:18"
            ),
            com.imhungry.sillok.presentation.state.meeting.SummaryUi(
                content = "1차 마일스톤: 로그인/회원/기본 목록.",
                timestamp = "00:00:40"
            ),
            com.imhungry.sillok.presentation.state.meeting.SummaryUi(
                content = "프로젝트 목표와 범위 합의 완료.",
                timestamp = "00:00:57"
            ),
            com.imhungry.sillok.presentation.state.meeting.SummaryUi(
                content = "1차 마일스톤: 로그인/회원/기본 목록.",
                timestamp = "00:01:14"
            ),
            com.imhungry.sillok.presentation.state.meeting.SummaryUi(
                content = "프로젝트 목표와 범위 합의 완료.",
                timestamp = "00:01:24"
            ),
            com.imhungry.sillok.presentation.state.meeting.SummaryUi(
                content = "1차 마일스톤: 로그인/회원/기본 목록.",
                timestamp = "00:01:40"
            )
        )

        val dummyFeedbacks = listOf(
            com.imhungry.sillok.presentation.state.meeting.FeedbackUi(
                comment = "회의 종료까지 10분 남았습니다.",
                timestamp = "00:02:00",
                isRead = true
            ),
            com.imhungry.sillok.presentation.state.meeting.FeedbackUi(
                comment = "다음 안건으로 넘어가 주세요.",
                timestamp = "00:02:20",
                isRead = true
            ),
            com.imhungry.sillok.presentation.state.meeting.FeedbackUi(
                comment = "회의 종료까지 10분 남았습니다.",
                timestamp = "00:02:40",
                isRead = true
            ),
            com.imhungry.sillok.presentation.state.meeting.FeedbackUi(
                comment = "다음 안건으로 넘어가 주세요.",
                timestamp = "00:03:10",
                isRead = true
            ),
            com.imhungry.sillok.presentation.state.meeting.FeedbackUi(
                comment = "회의 종료까지 10분 남았습니다.",
                timestamp = "00:03:20",
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
                actualStartTime = actualStartTime,
                actualEndTime = actualEndTime,
                actualDurationMinutes = actualDurationMinutes,
                participationRates = dummyParticipation.sortedByDescending { it.rate },
                segments = dummySegments,
                summaries = dummySummaries,
                recapSummary = "목표/범위 합의, 1차 마일스톤 정의",
                audio = AudioInfo(1, "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"),
                isLoading = false,
                error = null
            )
        }
        }
    }
}
