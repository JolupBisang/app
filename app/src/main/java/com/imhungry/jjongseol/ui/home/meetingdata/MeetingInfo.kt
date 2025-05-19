package com.imhungry.jjongseol.ui.home.meetingdata

import java.time.LocalDateTime

data class MeetingInfo(
    val title: String,
    val startDateTime: LocalDateTime,
    val endDateTime: LocalDateTime
)