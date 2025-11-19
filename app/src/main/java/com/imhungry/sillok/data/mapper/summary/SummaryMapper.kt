package com.imhungry.sillok.data.mapper.summary

import android.os.Build
import androidx.annotation.RequiresApi
import com.imhungry.sillok.data.model.summary.SummaryItemDto
import com.imhungry.sillok.domain.model.summary.Summary
import com.imhungry.sillok.presentation.util.DateTimeUtils
import javax.inject.Inject

class SummaryMapper @Inject constructor() {
    @RequiresApi(Build.VERSION_CODES.O)
    fun toDomain(dto: SummaryItemDto): Summary {
        return Summary(
            id = dto.id,
            content = dto.content,
            isRecap = dto.isRecap,
            generatedDateTime = dto.generatedDateTime
        )
    }
}
