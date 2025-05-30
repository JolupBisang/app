package com.imhungry.jjongseol.data.model.meeting.response

data class MeetingDetailRes(
    val meetingId: Long,
    val title: String,
    val location: String,
    val scheduledStartTime: String,
    val targetTime: Int,
    val restInterval: Int,
    val restDuration: Int,
    val meetingStatus: String
)
