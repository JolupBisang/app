package com.imhungry.sillok.domain.usecase.feedback

import com.imhungry.sillok.domain.repository.feedback.FeedbackRepository
import javax.inject.Inject

class GetFeedbacksUseCase @Inject constructor(
    private val repository: FeedbackRepository
) {
    suspend operator fun invoke(
        meetingId: Long,
        page: Int = 0,
        size: Int = 30
    ) = repository.getFeedbacks(meetingId, page, size)
}
