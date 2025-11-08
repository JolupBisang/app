package com.imhungry.sillok.presentation.state.meetingminutes

import com.imhungry.sillok.domain.model.agenda.Agenda
import com.imhungry.sillok.domain.model.audio.AudioInfo
import com.imhungry.sillok.domain.model.meeting.Meeting
import com.imhungry.sillok.domain.model.participation.UserParticipationRate
import com.imhungry.sillok.presentation.state.meeting.FeedbackUi
import com.imhungry.sillok.presentation.state.meeting.SegmentUi
import com.imhungry.sillok.presentation.state.meeting.SummaryUi

data class MeetingMinutesState(
    val meetingId: Long = 1L,
    val meetingDateAndLocation: String = "",
    val meetingTitle: String = "",
    val agendas: List<Agenda> = emptyList(),
    val feedbacks: List<FeedbackUi> = emptyList(),
    val scheduledStartTime: String = "",
    val scheduledEndTime: String = "",
    val targetTime: Int = 0,
    val actualStartTime: String = "",
    val actualEndTime: String = "",
    val actualDurationMinutes: Long = 0L,
    val participationRates: List<UserParticipationRate> = emptyList(),
    val segments: List<SegmentUi> = emptyList(),
    val summaries: List<SummaryUi> = emptyList(),
    val recapSummary: String = "",
    val audio: AudioInfo = AudioInfo(
        userId = 1L,
        presignedUrl = ""
    ),
    val isLoading: Boolean = false,
    val error: String? = null,
    // 페이징 관련 상태
    val segmentsPage: Int = 0,
    val summariesPage: Int = 0,
    val feedbacksPage: Int = 0,
    val hasMoreSegments: Boolean = true,
    val hasMoreSummaries: Boolean = true,
    val hasMoreFeedbacks: Boolean = true,
    val isLoadingMoreSegments: Boolean = false,
    val isLoadingMoreSummaries: Boolean = false,
    val isLoadingMoreFeedbacks: Boolean = false
)