package com.imhungry.jjongseol.data.repository.event

import com.imhungry.jjongseol.data.model.feedback.dto.FeedbackDto
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedbackEventRepository @Inject constructor() {
    private val _feedbackFlow = MutableSharedFlow<FeedbackDto>(replay = 0)
    val feedbackFlow: SharedFlow<FeedbackDto> = _feedbackFlow

    suspend fun emitFeedback(feedback: FeedbackDto) {
        _feedbackFlow.emit(feedback)
    }
}
