package com.imhungry.sillok.data.model.team

data class TeamListResDto(
    val teamId: Long,
    val teamName: String,
    val meetingName: String,
    val scheduledStartTime: String,
    val scheduledEndTime: String
)

