package com.imhungry.sillok.data.model.meeting

data class MeetingDetailSummaryResDto(
    val id: Long,
    val title: String,
    val scheduledStartTime: String,
    val targetTime: Int,
    val status: String
)
