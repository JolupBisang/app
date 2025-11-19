package com.imhungry.sillok.domain.repository.segment

import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.model.segment.Segment

interface SegmentRepository {
    suspend fun getSegments(
        meetingId: Long,
        page: Int = 0,
        size: Int = 40,
        direction: String
    ): ApiResult<List<Segment>>
    
    /**
     * 세그먼트 총 개수 조회
     * @return totalElements가 있으면 그 값을, 없으면 null 반환
     */
    suspend fun getTotalSegmentCount(meetingId: Long): ApiResult<Long?>
}
