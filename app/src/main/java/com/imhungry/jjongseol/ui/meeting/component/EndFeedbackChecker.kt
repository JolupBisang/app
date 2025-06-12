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

    // **1분 전 알림**
    LaunchedEffect(meetingDetail, startTime, feedbackList) {
        while (true) {
            val now = System.currentTimeMillis()
            val oneMinNoticeTime = endMillis - 1 * 60_000L // 1분(60,000ms) 전
            val alreadyAdded1m = feedbackList.any {
                it.comment.startsWith("회의 종료까지 1분 남았습니다.") &&
                        it.timestamp == millisToIso(oneMinNoticeTime)
            }
            if (now in (oneMinNoticeTime..oneMinNoticeTime + 1_000L) && !alreadyAdded1m) {
                val endTimeText = millisToKoreanTimeString(endMillis)
                onAddFeedback(
                    FeedbackDto(
                        timestamp = millisToTimeString(oneMinNoticeTime),
                        comment = "회의 종료까지 1분 남았습니다.\n예정 종료 시각: $endTimeText"
                    )
                )
            }

            delay(1_000L)
        }
    }
}

