package com.imhungry.sillok.presentation.state.meeting

import com.imhungry.sillok.domain.model.agenda.Agenda
import com.imhungry.sillok.domain.model.participation.UserParticipationRate

data class FeedbackUi(
    val comment: String,
    val timestamp: String,
    val isRead: Boolean = false,
    val millis: Long = 0L
)

data class SummaryUi(
    val content: String,
    val timestamp: String,
    val millis: Long = 0L
)

data class SegmentUi(
    val order: Int,
    val timestamp: String,
    val text: String,
    val nickname: String,
    val profileImage: String,
    val isFromCurrentUser: Boolean,
    val isSameAsPrevious: Boolean,
    val isSameAsNext: Boolean,
    val millis: Long = 0L
)

data class MeetingInProgressState(
    val meetingId: Long = 1L,
    val agendas: List<Agenda> = emptyList(),
    val feedbacks: List<FeedbackUi> = emptyList(),
    val participationRates: List<UserParticipationRate> = emptyList(),
    val segments: List<SegmentUi> = emptyList(),
    val summaries: List<SummaryUi> = emptyList(),
    val startTime: Long = 0L,
    val targetTime: Int = 0, // 목표 시간 (분)
    val restInterval: Int = 0, // 휴식 간격 (분)
    val restDuration: Int = 0, // 휴식 시간 (분)
    val isHost: Boolean = false,
    val isLoading: Boolean = false,
    val isMicLoading: Boolean = false, // 마이크 켜지는 중 로딩 상태
    val error: String? = null
)