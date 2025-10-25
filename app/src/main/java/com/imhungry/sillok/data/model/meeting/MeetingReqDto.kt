package com.imhungry.sillok.data.model.meeting

data class MeetingReqDto(
    val title: String,
    val location: String,
    val scheduledStartTime: String,
    val targetTime: Int,
    val restInterval: Int,
    val restDuration: Int,
    val participants: List<String>,
    val agendas: List<String>
)
