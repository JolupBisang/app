package com.imhungry.sillok.data.model.meeting

data class DuplicationCheckRes(
    val meetings: List<MeetingDetail>
)

data class MeetingDetail(
    val title: String,
    val scheduledStartTime: String,
    val scheduledEndTime: String
)