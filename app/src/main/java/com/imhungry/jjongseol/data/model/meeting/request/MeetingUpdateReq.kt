package com.imhungry.jjongseol.data.model.meeting.request

data class MeetingUpdateReq(
    val title: String,
    val location: String,
    val scheduledStartTime: String,
    val targetTime: Int,
    val restInterval: Int,
    val restDuration: Int,
    val agendas: List<String>
)
