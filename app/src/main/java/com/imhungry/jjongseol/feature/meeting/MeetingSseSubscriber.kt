package com.imhungry.jjongseol.feature.meeting

import com.imhungry.jjongseol.data.model.feedback.FeedbackItem
import com.imhungry.jjongseol.data.model.meeting.SummaryItem
import com.imhungry.jjongseol.data.network.client.SseClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class MeetingSseSubscriber @Inject constructor(
    private val sseClient: SseClient
) {
    private val _summaryList = MutableStateFlow<List<SummaryItem>>(emptyList())
    val summaryList: StateFlow<List<SummaryItem>> = _summaryList.asStateFlow()

    private val _participationRate = MutableStateFlow<String?>(null)
    val participationRate: StateFlow<String?> = _participationRate.asStateFlow()

    private val _feedbackList = MutableStateFlow<List<FeedbackItem>>(emptyList())
    val feedbackList: StateFlow<List<FeedbackItem>> = _feedbackList.asStateFlow()

    fun subscribeToSummary(meetingId: Long, timeProvider: () -> String) {
        sseClient.subscribeToEvent(
            endpoint = "summary",
            meetingId = meetingId,
            eventType = "SUMMARY"
        ) {
            _summaryList.value += SummaryItem(it.trim('"'), timeProvider())
        }
    }

    fun subscribeToParticipationRate(meetingId: Long) {
        sseClient.subscribeToEvent(
            endpoint = "participation_rate",
            meetingId = meetingId,
            eventType = "PARTICIPATION_RATE"
        ) {
            _participationRate.value = it
        }
    }

    fun subscribeToFeedback(meetingId: Long, timeProvider: () -> String) {
        sseClient.subscribeToEvent(
            endpoint = "feedback",
            meetingId = meetingId,
            eventType = "FEEDBACK"
        ) {
            _feedbackList.value += FeedbackItem(it.trim('"'), timeProvider())
        }
    }

    fun markAllFeedbackAsRead() {
        _feedbackList.value = _feedbackList.value.map {
            if (!it.isRead) it.copy(isRead = true) else it
        }
    }

    fun stopSse() {
        sseClient.disconnect()
    }
}
