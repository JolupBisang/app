package com.imhungry.sillok.data.mapper.feedback

import com.imhungry.sillok.data.model.feedback.FeedbackItemDto
import com.imhungry.sillok.domain.model.feedback.Feedback
import javax.inject.Inject

class FeedbackMapper @Inject constructor() {
    fun toDomain(dto: FeedbackItemDto): Feedback {
        return Feedback(
            id = dto.id,
            comment = dto.comment,
            generatedDateTime = dto.generatedDateTime.take(19)
        )
    }
}
