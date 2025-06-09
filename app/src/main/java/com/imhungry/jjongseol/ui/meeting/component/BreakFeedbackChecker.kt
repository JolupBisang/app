package com.imhungry.jjongseol.ui.meeting.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.imhungry.jjongseol.data.model.feedback.dto.FeedbackDto
import com.imhungry.jjongseol.data.model.meeting.response.MeetingDetailRes
import kotlinx.coroutines.delay

data class BreakTime(
    val breakIndex: Int,
    val startMillis: Long,
    val endMillis: Long
)

fun getBreakSchedule(
    startTime: Long,
    targetTime: Int,
    restInterval: Int,
    restDuration: Int
): List<BreakTime> {
    val schedule = mutableListOf<BreakTime>()
    var cur = restInterval
    var idx = 1
    while (cur < targetTime) {
        val breakStart = startTime + cur * 60_000L
        val breakEnd = breakStart + restDuration * 60_000L
        schedule.add(BreakTime(idx, breakStart, breakEnd))
        cur += restInterval
        idx++
    }
    return schedule
}

@Composable
fun BreakFeedbackChecker(
    meetingId: Long,
    meetingDetail: MeetingDetailRes?,
    startTime: Long?,
    feedbackList: List<FeedbackDto>,
    onAddFeedback: (FeedbackDto) -> Unit
) {
    if (meetingDetail == null || startTime == null) return
    val breaks = remember(meetingDetail, startTime) {
        getBreakSchedule(
            startTime,
            meetingDetail.targetTime,
            meetingDetail.restInterval,
            meetingDetail.restDuration
        )
    }

    LaunchedEffect(breaks, feedbackList) {
        while (true) {
            val now = System.currentTimeMillis()
            for (b in breaks) {
                val noticeTime = b.startMillis - 60_000L // 1분 전
                val alreadyAdded = feedbackList.any {
                    it.comment.startsWith("잠시 후 휴식 시간입니다.") &&
                            it.timestamp == millisToIso(noticeTime)
                }
                if (now in (noticeTime..noticeTime+1_000L) && !alreadyAdded) {
                    // 알림 추가
                    val startTimeText = millisToKoreanTimeString(b.startMillis)
                    val endTimeText = millisToKoreanTimeString(b.endMillis)
                    onAddFeedback(
                        FeedbackDto(
                            timestamp = millisToIso(noticeTime),
                            comment = "잠시 후 휴식 시간입니다.\n쉬는 시간: $startTimeText - $endTimeText"
                        )
                    )
                }
            }
            delay(1_000L)
        }
    }
}

fun millisToIso(millis: Long): String =
    java.time.Instant.ofEpochMilli(millis)
        .atZone(java.time.ZoneId.systemDefault())
        .toLocalDateTime()
        .format(java.time.format.DateTimeFormatter.ISO_DATE_TIME)

fun millisToTimeString(millis: Long): String =
    java.time.Instant.ofEpochMilli(millis)
        .atZone(java.time.ZoneId.systemDefault())
        .toLocalTime()
        .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"))

fun millisToKoreanTimeString(millis: Long): String {
    val localTime = java.time.Instant.ofEpochMilli(millis)
        .atZone(java.time.ZoneId.systemDefault())
        .toLocalTime()
    val hour = localTime.hour
    val minute = localTime.minute
    return "${hour}시 ${minute}분"
}
