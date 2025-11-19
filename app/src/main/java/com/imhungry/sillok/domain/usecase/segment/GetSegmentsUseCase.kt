package com.imhungry.sillok.domain.usecase.segment

import com.imhungry.sillok.domain.repository.segment.SegmentRepository
import javax.inject.Inject

class GetSegmentsUseCase @Inject constructor(
    private val repository: SegmentRepository
) {
    suspend operator fun invoke(
        meetingId: Long,
        page: Int = 0,
        size: Int = 40,
        direction: String = "desc"
    ) = repository.getSegments(meetingId, page, size, direction)
}
