package com.imhungry.sillok.presentation.state.home

import com.imhungry.sillok.domain.model.meeting.MeetingDetailSummary

data class OngoingMeeting(
    val id: Long,
    val title: String,
    val scheduledStartTime: String,
    val scheduledEndTime: String
) {
    val formattedTime: String
        get() {
            val startDate = scheduledStartTime.split("T", " ")[0].replace("-", ".")
            val startTime = scheduledStartTime.split("T", " ")[1].substring(0, 5)
            val endTime = scheduledEndTime.split("T", " ")[1].substring(0, 5)
            return "$startDate  $startTime~$endTime"
        }
}

data class HomeState(
    val userName: String = "",
    val profileImage: String = "",
    val scheduledMeetings: List<MeetingDetailSummary> = emptyList(),
    val pastMeetings: List<MeetingDetailSummary> = emptyList(),
    val ongoingMeetings: List<OngoingMeeting> = emptyList(),
    val searchText: String = "",
    val searchResults: List<MeetingDetailSummary> = emptyList(),
    val isSearching: Boolean = false,
    val hasMeeting: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
) 