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
        presignedUrl = "https://bucket-silrok.s3.ap-northeast-2.amazonaws.com/merged-audio/meeting-10/user-1/merged.opus?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20251109T201103Z&X-Amz-SignedHeaders=host&X-Amz-Credential=AKIA23WHUNUR3LDEBZAX%2F20251109%2Fap-northeast-2%2Fs3%2Faws4_request&X-Amz-Expires=86400&X-Amz-Signature=374eba12fa56586a9c84d770f50fb96e725e784042a1341b4d8dca55eba59c19"
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