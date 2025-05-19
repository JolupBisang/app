package com.imhungry.jjongseol.data.model.home

data class MeetingListWrapper(
    val message: String,
    val data: MeetingListData
)

data class MeetingListData(
    val meetings: List<MeetingResponse>
)
