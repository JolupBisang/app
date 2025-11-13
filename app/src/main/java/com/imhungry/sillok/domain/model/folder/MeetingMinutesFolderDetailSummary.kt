package com.imhungry.sillok.domain.model.folder

data class MeetingMinutesFolderDetailSummary(
    val id: Long,
    val name: String,
    val meetingName: String?,
    val date: String,
    val timeRange: String,
    val isPast: Boolean
)