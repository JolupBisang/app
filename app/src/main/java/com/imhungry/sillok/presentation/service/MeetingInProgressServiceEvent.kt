package com.imhungry.sillok.presentation.service

import com.imhungry.sillok.data.model.realtime.ErrorResponse
import com.imhungry.sillok.data.model.realtime.LiveFeedbackDto
import com.imhungry.sillok.data.model.realtime.LiveSummaryDto
import com.imhungry.sillok.data.model.realtime.RealtimeSegmentDto

/**
 * Service 이벤트 타입 정의
 */
sealed class ServiceEvent {
    data class ConnectionEstablished(val lastProcessedChunkId: Long?) : ServiceEvent()
    data class DiarizedSegment(val segment: RealtimeSegmentDto) : ServiceEvent()
    data object CompletionScheduled : ServiceEvent()
    data class MeetingCompleted(val message: String?) : ServiceEvent()
    data class ParticipationRate(val rates: Map<Long, Double>) : ServiceEvent()
    data class Feedback(val feedback: LiveFeedbackDto) : ServiceEvent()
    data class Summary(val summary: LiveSummaryDto) : ServiceEvent()
    data class Error(val error: ErrorResponse?) : ServiceEvent()
    data object MicEnabled : ServiceEvent()
    data class AgendaUpdated(val agendaId: Long, val isCompleted: Boolean) : ServiceEvent()
}

