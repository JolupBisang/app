package com.imhungry.sillok.presentation.state.meeting

import com.imhungry.sillok.domain.model.agenda.Agenda
import com.imhungry.sillok.domain.model.participation.UserParticipationRate

data class FeedbackUi(
    val comment: String,
    val timestamp: String,
    val isRead: Boolean = false
)

data class SummaryUi(
    val content: String,
    val timestamp: String
)

data class SegmentUi(
    val timestamp: String,
    val text: String,
    val nickname: String,
    val profileImage: String,
    val isFromCurrentUser: Boolean,
    val isSameAsPrevious: Boolean,
    val isSameAsNext: Boolean
)

data class MeetingInProgressState(
    val meetingId: Long = 1L,
    val agendas: List<Agenda> = emptyList(),
    val feedbacks: List<FeedbackUi> = emptyList(),
    val participationRates: List<UserParticipationRate> = emptyList(),
    val segments: List<SegmentUi> = emptyList(),
    val summaries: List<SummaryUi> = emptyList(),
    val startTime: Long = 0L,
    val isLoading: Boolean = false,
    val error: String? = null
)