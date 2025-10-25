package com.imhungry.sillok.domain.repository.feedback

import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.model.feedback.Feedback

interface FeedbackRepository {
    suspend fun getFeedbacks(
        meetingId: Long,
        page: Int = 0,
        size: Int = 30
    ): ApiResult<List<Feedback>>
}
