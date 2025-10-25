package com.imhungry.sillok.data.model.segment

data class SegmentListResDto(
    val id: Long,
    val userId: Long,
    val userName: String,
    val segmentOrder: Int,
    val timestamp: String,
    val text: String,
    val lang: String
)
