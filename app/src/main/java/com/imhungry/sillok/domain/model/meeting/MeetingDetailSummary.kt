package com.imhungry.sillok.domain.model.meeting

data class MeetingDetailSummary(
    val id: Long,
    val title: String,
    val scheduledStartTime: String,
    val targetTime: Int,
    val status: String
)