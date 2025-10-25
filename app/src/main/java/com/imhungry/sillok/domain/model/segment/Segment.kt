package com.imhungry.sillok.domain.model.segment

data class Segment(
    val id: Long,
    val userId: Long,
    val userName: String,
    val segmentOrder: Int,
    val timestamp: String,
    val text: String,
    val lang: String
)
