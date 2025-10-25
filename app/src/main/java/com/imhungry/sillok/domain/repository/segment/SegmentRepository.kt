package com.imhungry.sillok.domain.repository.segment

import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.model.segment.Segment

interface SegmentRepository {
    suspend fun getSegments(
        meetingId: Long,
        page: Int = 0,
        size: Int = 40
    ): ApiResult<List<Segment>>
}
