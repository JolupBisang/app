package com.imhungry.jjongseol.data.model

data class MeetingReq(
    val title: String,
    val leader: String,
    val location: String,
    val targetTime: Int,
    val restInterval: Int,
    val scheduledStartTime: String,
    val agendas: List<String>,
    val participants: List<String>
)