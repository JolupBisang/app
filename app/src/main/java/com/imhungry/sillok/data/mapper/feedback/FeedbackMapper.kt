package com.imhungry.sillok.data.mapper.feedback

import android.os.Build
import androidx.annotation.RequiresApi
import com.imhungry.sillok.data.model.feedback.FeedbackItemDto
import com.imhungry.sillok.domain.model.feedback.Feedback
import com.imhungry.sillok.presentation.util.DateTimeUtils
import javax.inject.Inject

class FeedbackMapper @Inject constructor() {
    @RequiresApi(Build.VERSION_CODES.O)
    fun toDomain(dto: FeedbackItemDto): Feedback {
        return Feedback(
            id = dto.id,
            comment = dto.comment,
            generatedDateTime = dto.generatedDateTime.take(19)
        )
    }
}
