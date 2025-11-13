package com.imhungry.sillok.data.mapper.segment

import android.os.Build
import androidx.annotation.RequiresApi
import com.imhungry.sillok.data.model.segment.SegmentItemDto
import com.imhungry.sillok.domain.model.segment.Segment
import com.imhungry.sillok.presentation.util.DateTimeUtils
import javax.inject.Inject

class SegmentMapper @Inject constructor() {
    @RequiresApi(Build.VERSION_CODES.O)
    fun toDomain(dto: SegmentItemDto): Segment {
        return Segment(
            id = dto.id,
            userId = dto.userId,
            segmentOrder = dto.segmentOrder,
            timestamp = dto.timestamp.take(19),
            text = dto.text,
            lang = dto.lang
        )
    }
}
