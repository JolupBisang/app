package com.imhungry.jjongseol.ui.meeting.component

import androidx.compose.runtime.*
import com.imhungry.jjongseol.data.model.feedback.dto.FeedbackDto
import com.imhungry.jjongseol.data.model.meeting.response.MeetingDetailRes
import kotlinx.coroutines.delay

@Composable
fun EndFeedbackChecker(
    meetingId: Long,
    meetingDetail: MeetingDetailRes?,
    startTime: Long?,
    feedbackList: List<FeedbackDto>,
    onAddFeedback: (FeedbackDto) -> Unit,
) {
    if (meetingDetail == null || startTime == null) return

    val endMillis = startTime + (meetingDetail.targetTime * 60_000L)

    // 10분 전 알림
    LaunchedEffect(meetingDetail, startTime, feedbackList) {
        while (true) {
            val now = System.currentTimeMillis()
            val tenMinNoticeTime = endMillis - 10 * 60_000L
            val alreadyAdded10m = feedbackList.any {
                it.comment.startsWith("회의 종료까지 10분 남았습니다.") &&
                        it.timestamp == millisToIso(tenMinNoticeTime)
            }
            if (now in (tenMinNoticeTime..tenMinNoticeTime + 1_000L) && !alreadyAdded10m) {
                val endTimeText = millisToKoreanTimeString(endMillis)
                onAddFeedback(
                    FeedbackDto(
                        timestamp = millisToTimeString(tenMinNoticeTime),
                        comment = "회의 종료까지 10분 남았습니다.\n예정 종료 시각: $endTimeText"
                    )
                )
            }

            delay(1_000L)
        }
    }
}

