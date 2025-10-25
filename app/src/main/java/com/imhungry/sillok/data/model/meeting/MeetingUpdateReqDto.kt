package com.imhungry.sillok.data.model.meeting

data class MeetingUpdateReqDto(
    val title: String,
    val location: String,
    val scheduledStartTime: String,
    val targetTime: Int,
    val restInterval: Int,
    val restDuration: Int,
    val agendas: List<String>
)
