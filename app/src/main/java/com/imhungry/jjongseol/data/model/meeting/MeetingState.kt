package com.imhungry.jjongseol.data.model.meeting

data class MeetingState(
    val meetingId: Long,
    val micEnabled: Boolean,
    val startTime: Long,
    val endTime: Long
)