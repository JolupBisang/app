package com.imhungry.jjongseol.data.model.home

import com.imhungry.jjongseol.ui.home.meetingdata.MeetingInfo
import java.time.LocalDateTime

data class MeetingResponse(
    val id: Long,
    val title: String,
    val scheduledStartTime: String,
    val targetTime: Int,
    val status: String
)

fun MeetingResponse.toMeetingInfo(): MeetingInfo {
    val start = LocalDateTime.parse(scheduledStartTime)
    val end = start.plusMinutes(targetTime.toLong())
    return MeetingInfo(id, title, start, end)
}