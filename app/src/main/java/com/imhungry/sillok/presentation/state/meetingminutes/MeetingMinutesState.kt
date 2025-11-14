package com.imhungry.sillok.presentation.state.meetingminutes

import com.imhungry.sillok.domain.model.agenda.Agenda
import com.imhungry.sillok.domain.model.audio.AudioInfo
import com.imhungry.sillok.domain.model.participation.UserParticipationRate
import com.imhungry.sillok.presentation.state.meeting.FeedbackUi
import com.imhungry.sillok.presentation.state.meeting.SegmentUi
import com.imhungry.sillok.presentation.state.meeting.SummaryUi

data class MeetingMinutesState(
    val meetingId: Long = 1L,
    val meetingDateAndLocation: String = "",
    val meetingTitle: String = "",
    val agendas: List<Agenda> = emptyList(),
    val scheduledStartTime: String = "",
    val scheduledEndTime: String = "",
    val targetTime: Int = 0,
    val actualStartTime: String = "",
    val actualEndTime: String = "",
    val actualDurationMinutes: Long = 0L,
    val participationRates: List<UserParticipationRate> = emptyList(),
    val recapSummary: String = "",
    val audio: AudioInfo = AudioInfo(
        userId = 1L,
        presignedUrl = ""
    ),
    val isLoading: Boolean = false,
    val error: String? = null,
    val startMillis: Long? = null,
    val currentUserId: Long? = null
)