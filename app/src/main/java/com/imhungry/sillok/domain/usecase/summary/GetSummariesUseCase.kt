package com.imhungry.sillok.domain.usecase.summary

import com.imhungry.sillok.domain.repository.summary.SummaryRepository
import javax.inject.Inject

class GetSummariesUseCase @Inject constructor(
    private val repository: SummaryRepository
) {
    suspend operator fun invoke(
        meetingId: Long,
        isRecap: Boolean = false,
        page: Int = 0,
        size: Int = 30
    ) = repository.getSummaries(meetingId, isRecap, page, size)
}
