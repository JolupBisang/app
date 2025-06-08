package com.imhungry.jjongseol.data.model.segment.response

data class SegmentListRes(
    val id: Long,
    val userId: Long,
    val userName: String,
    val segmentOrder: Int,
    val timestamp: String,
    val text: String,
    val lang: String?
)