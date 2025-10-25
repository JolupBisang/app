package com.imhungry.sillok.data.mapper.segment

import com.imhungry.sillok.data.model.segment.SegmentListResDto
import com.imhungry.sillok.domain.model.segment.Segment
import javax.inject.Inject

class SegmentMapper @Inject constructor() {
    fun toDomain(dto: SegmentListResDto): Segment {
        return Segment(
            id = dto.id,
            userId = dto.userId,
            userName = dto.userName,
            segmentOrder = dto.segmentOrder,
            timestamp = dto.timestamp,
            text = dto.text,
            lang = dto.lang
        )
    }
}