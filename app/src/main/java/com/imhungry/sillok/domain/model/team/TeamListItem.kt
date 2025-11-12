package com.imhungry.sillok.domain.model.team

data class TeamListItem(
    val teamId: Long,
    val teamName: String,
    val meetingName: String?,
    val scheduledStartTime: String?,
    val scheduledEndTime: String?
)

