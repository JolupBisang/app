package com.imhungry.sillok.data.mapper.summary

import com.imhungry.sillok.data.model.summary.SummaryListResDto
import com.imhungry.sillok.domain.model.summary.Summary
import javax.inject.Inject

class SummaryMapper @Inject constructor() {
    fun toDomain(dto: SummaryListResDto): Summary {
        return Summary(
            id = dto.id,
            content = dto.content,
            isRecap = dto.isRecap,
            generatedDateTime = dto.generatedDateTime.take(19)
        )
    }
}