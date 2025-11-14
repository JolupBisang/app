package com.imhungry.sillok.presentation.state.home

import android.os.Build
import androidx.annotation.RequiresApi
import com.imhungry.sillok.domain.model.meeting.MeetingDetailSummary
import com.imhungry.sillok.presentation.util.DateTimeUtils
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
data class MeetingUi(
    val id: Long,
    val title: String,
    val status: String,
    val scheduledStartTime: String,
    val formattedTime: String,
    val timeRange: String,
    val dismissed: Boolean = false
) {
    companion object {
        fun from(meeting: MeetingDetailSummary): MeetingUi {

            return try {
                val endTime = DateTimeUtils.calcEndDate(meeting.scheduledStartTime, meeting.targetTime)

                val date = DateTimeUtils.localIsoToDateString(meeting.scheduledStartTime)
                val start = DateTimeUtils.localIsoToTimeString(meeting.scheduledStartTime)
                val end = DateTimeUtils.localIsoToTimeString(endTime)

                MeetingUi(
                    id = meeting.id,
                    title = meeting.title,
                    status = meeting.status,
                    scheduledStartTime = meeting.scheduledStartTime,
                    formattedTime = "$date   $start~$end",
                    timeRange = "$start - $end"
                )
            } catch (e: Exception) {
                MeetingUi(
                    id = meeting.id,
                    title = meeting.title,
                    status = meeting.status,
                    scheduledStartTime = meeting.scheduledStartTime,
                    formattedTime = "시간 미정",
                    timeRange = "시간 미정"
                )
            }
        }
    }
}

data class HomeState(
    val userName: String = "",
    val profileImage: String = "",
    val meetings: List<MeetingUi> = emptyList(),
    val ongoingMeetings: List<MeetingUi> = emptyList(),
    val upcomingMeetings: List<MeetingUi> = emptyList(),
    val searchText: String = "",
    val searchQuery: String = "", // 실제 검색에 사용되는 쿼리
    val searchResults: List<MeetingUi> = emptyList(),
    val isSearching: Boolean = false,
    val hasNewMeeting: Boolean = false,
    val startedMeetingId: Long? = null,
    val showMeetingStartedDialog: Boolean = false,
    val pendingMeetingId: Long? = null,
    val pendingMeetingTitle: String? = null,
    val showExitDialog: Boolean = false,
    val showGeneratingMeetingNoteDialog: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
) 