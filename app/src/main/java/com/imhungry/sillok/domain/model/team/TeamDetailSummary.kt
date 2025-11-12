package com.imhungry.sillok.domain.model.team

data class TeamDetailSummary(
    val id: Long,
    val name: String,
    val memberCount: Int,
    val date: String?,
    val timeRange: String?,
    val meetingName: String?,
    val isPast: Boolean = false
)