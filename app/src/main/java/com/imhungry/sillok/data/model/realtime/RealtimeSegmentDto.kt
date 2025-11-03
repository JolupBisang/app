package com.imhungry.sillok.data.model.realtime

data class RealtimeSegmentDto(
    val timestamp: String,
    val userId: Long,
    val order: Int,
    val text: String
)