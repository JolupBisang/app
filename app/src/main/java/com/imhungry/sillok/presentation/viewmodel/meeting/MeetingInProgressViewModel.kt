package com.imhungry.sillok.presentation.viewmodel.meeting

import android.app.Application
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.imhungry.sillok.data.local.UserStore
import com.imhungry.sillok.data.remote.realtime.RealtimeEvent
import com.imhungry.sillok.data.remote.realtime.RealtimeEventBus
import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.model.participation.UserParticipationRate
import com.imhungry.sillok.domain.usecase.agenda.GetAgendasUseCase
import com.imhungry.sillok.domain.usecase.feedback.GetFeedbacksUseCase
import com.imhungry.sillok.domain.usecase.participation.GetParticipationRateHistoryUseCase
import com.imhungry.sillok.domain.usecase.segment.GetSegmentsUseCase
import com.imhungry.sillok.domain.usecase.summary.GetSummariesUseCase
import com.imhungry.sillok.presentation.service.RealtimeService
import com.imhungry.sillok.presentation.state.meeting.FeedbackUi
import com.imhungry.sillok.presentation.state.meeting.MeetingInProgressState
import com.imhungry.sillok.presentation.state.meeting.SegmentUi
import com.imhungry.sillok.presentation.state.meeting.SummaryUi
import com.imhungry.sillok.presentation.util.DateTimeUtils
import com.imhungry.sillok.presentation.util.ProfileUtils
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
class MeetingInProgressViewModel @Inject constructor(
    private val getAgendasUseCase: GetAgendasUseCase,
    private val getSegmentsUseCase: GetSegmentsUseCase,
    private val getSummariesUseCase: GetSummariesUseCase,
    private val getParticipationRateHistoryUseCase: GetParticipationRateHistoryUseCase,
    private val getFeedbacksUseCase: GetFeedbacksUseCase,
    private val userStore: UserStore,
    private val app: Application,
    private val realtimeEventBus: RealtimeEventBus
) : ViewModel() {
    companion object {
        private const val TAG = "MeetingInProgressViewModel"
    }

    private val _state = MutableStateFlow(MeetingInProgressState())
    val state: StateFlow<MeetingInProgressState> = _state.asStateFlow()

    private val _micEnabled = MutableStateFlow(true)
    val micEnabled: StateFlow<Boolean> = _micEnabled.asStateFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    fun initialize(meetingId: Long) {
        _state.update { it.copy(meetingId = meetingId) }
        //refreshAll()
        loadDummyMeetingInProgressState()
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startRealtimeService(meetingId: Long) {
        val intent = Intent(app, RealtimeService::class.java).apply {
            putExtra(RealtimeService.EXTRA_MEETING_ID, meetingId)
        }
        app.startForegroundService(intent)
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



    private var observingRealtime = false
    private fun startObservingRealtimeEvents() {
        if (observingRealtime) return
        observingRealtime = true
        viewModelScope.launch {
            realtimeEventBus.events.collect { event ->
                when (event) {
                    is RealtimeEvent.Summary -> {
                        // 요약이 갱신되면 리스트에 추가합니다.
                        _state.update { st ->
                            val added = st.summaries + SummaryUi(
                                content = event.summary,
                                timestamp = event.timestamp
                            )
                            st.copy(summaries = added)
                        }
                    }
                    is RealtimeEvent.Feedback -> {
                        // 피드백이 수신되면 읽지 않음 상태로 리스트에 추가합니다.
                        _state.update { st ->
                            val added = st.feedbacks + FeedbackUi(
                                comment = event.comment,
                                timestamp = event.timestamp,
                                isRead = false
                            )
                            st.copy(feedbacks = added)
                        }
                    }
                    is RealtimeEvent.ParticipationRate -> {
                        // 사용자별 참여율 맵을 업데이트하고 내림차순 정렬합니다.
                        _state.update { st ->
                            val byUser = st.participationRates.associateBy { it.userId }.toMutableMap()
                            event.participationRates.forEach { item ->
                                val prev = byUser[item.userId]
                                val nickname = prev?.nickname ?: ""
                                byUser[item.userId] = UserParticipationRate(
                                    userId = item.userId,
                                    nickname = nickname,
                                    rate = item.rate
                                )
                            }
                            val updated = byUser.values.sortedByDescending { it.rate }
                            st.copy(participationRates = updated)
                        }
                    }

                    else -> {}
                }
            }
        }
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