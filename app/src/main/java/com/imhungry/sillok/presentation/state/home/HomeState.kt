package com.imhungry.sillok.presentation.state.home

import android.os.Build
import androidx.annotation.RequiresApi
import com.imhungry.sillok.domain.model.meeting.MeetingDetailSummary
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
            // 빈 문자열이거나 null인 경우
            if (meeting.scheduledStartTime.isBlank()) {
                return MeetingUi(
                    id = meeting.id,
                    title = meeting.title,
                    status = meeting.status,
                    scheduledStartTime = meeting.scheduledStartTime,
                    formattedTime = "시간 미정",
                    timeRange = "시간 미정"
                )
            }

            return try {
                // 다양한 날짜 형식 시도
                val startTime = parseDateTime(meeting.scheduledStartTime)
                val endTime = startTime.plusMinutes(meeting.targetTime.toLong())

                val startDate = meeting.scheduledStartTime.split("T", " ")[0].replace("-", ".")
                val startTimeFormatted = startTime.format(DateTimeFormatter.ofPattern("HH:mm"))
                val endTimeFormatted = endTime.format(DateTimeFormatter.ofPattern("HH:mm"))

                MeetingUi(
                    id = meeting.id,
                    title = meeting.title,
                    status = meeting.status,
                    scheduledStartTime = meeting.scheduledStartTime,
                    formattedTime = "$startDate   $startTimeFormatted~$endTimeFormatted",
                    timeRange = "$startTimeFormatted - $endTimeFormatted"
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

        private fun parseDateTime(dateTimeString: String): LocalDateTime {
            // ISO_LOCAL_DATE_TIME 형식 시도 (예: "2025-11-01T10:00:00")
            return try {
                LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            } catch (e: Exception) {
                // ISO_DATE_TIME 형식 시도 (예: "2025-11-01T10:00:00Z")
                try {
                    java.time.ZonedDateTime.parse(dateTimeString, DateTimeFormatter.ISO_DATE_TIME)
                        .toLocalDateTime()
                } catch (e2: Exception) {
                    // ISO_OFFSET_DATE_TIME 형식 시도 (예: "2025-11-01T10:00:00+09:00")
                    try {
                        java.time.OffsetDateTime.parse(
                            dateTimeString,
                            DateTimeFormatter.ISO_OFFSET_DATE_TIME
                        ).toLocalDateTime()
                    } catch (e3: Exception) {
                        // 커스텀 형식 시도 (예: "2025-11-01 10:00:00")
                        try {
                            LocalDateTime.parse(
                                dateTimeString,
                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                            )
                        } catch (e4: Exception) {
                            // 마지막 시도: "yyyy-MM-dd'T'HH:mm:ss" 형식
                            LocalDateTime.parse(
                                dateTimeString,
                                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                            )
                        }
                    }
                }
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