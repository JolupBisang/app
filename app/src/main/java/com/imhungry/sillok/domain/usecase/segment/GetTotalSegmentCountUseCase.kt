package com.imhungry.sillok.domain.usecase.segment

import com.imhungry.sillok.domain.repository.segment.SegmentRepository
import javax.inject.Inject

class GetTotalSegmentCountUseCase @Inject constructor(
    private val repository: SegmentRepository
) {
    suspend operator fun invoke(meetingId: Long) =
        repository.getTotalSegmentCount(meetingId)
}

