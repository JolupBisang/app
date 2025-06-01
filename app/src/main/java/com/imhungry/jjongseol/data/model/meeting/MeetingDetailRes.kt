package com.imhungry.jjongseol.data.model.meeting

data class MeetingDetailRes(
    val meetingId: Long,
    val title: String,
    val location: String,
    val scheduledStartTime: String,
    val targetTime: Int,
    val restInterval: Int,
    val restDuration: Int,
    val agendas: List<String>,
    val participants: List<ParticipantDto>,
    val status: String
)

data class ParticipantDto(
    val userId: Long,
    val email: String
)