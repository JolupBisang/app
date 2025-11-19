package com.imhungry.sillok.data.model.realtime

data class RealtimeSegmentDto(
    val spokenTime: String,
    val translatedTime: String,
    val userId: Long,
    val order: Int,
    val text: String
)