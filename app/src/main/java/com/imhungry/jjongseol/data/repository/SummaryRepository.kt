package com.imhungry.jjongseol.data.repository

import com.imhungry.jjongseol.data.model.meeting.dto.SummaryDto
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SummaryRepository @Inject constructor() {
    private val _summaryFlow = MutableSharedFlow<SummaryDto>(replay = 0)
    val summaryFlow: SharedFlow<SummaryDto> = _summaryFlow

    suspend fun emitSummary(summary: SummaryDto) {
        _summaryFlow.emit(summary)
    }
}