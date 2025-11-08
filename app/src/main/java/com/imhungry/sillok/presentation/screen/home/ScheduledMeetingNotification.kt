package com.imhungry.sillok.presentation.screen.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imhungry.sillok.presentation.state.home.MeetingUi
import com.imhungry.sillok.ui.components.SmallSillokButton
import com.imhungry.sillok.ui.theme.gray200
import com.imhungry.sillok.ui.theme.green200
import com.imhungry.sillok.ui.theme.green300
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@RequiresApi(Build.VERSION_CODES.O)
private fun calculateTimeUntil(scheduledStartTime: String): String {
    return try {
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        val startTime = LocalDateTime.parse(scheduledStartTime, formatter)
        val now = LocalDateTime.now()

        val minutesUntil = ChronoUnit.MINUTES.between(now, startTime)
        val hoursUntil = ChronoUnit.HOURS.between(now, startTime)
        val daysUntil = ChronoUnit.DAYS.between(now, startTime)

        when {
            minutesUntil < 0 -> "started"
            minutesUntil < 1 -> "starting soon"
            minutesUntil < 60 -> "$minutesUntil min${if (minutesUntil > 1) "s" else ""} left"
            hoursUntil < 24 -> "$hoursUntil hour${if (hoursUntil > 1) "s" else ""} left"
            else -> "$daysUntil day${if (daysUntil > 1) "s" else ""} left"
        }
    } catch (e: Exception) {
        "upcoming"
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ScheduledMeetingNotification(
    meetings: List<MeetingUi>,
    onJoinMeeting: (MeetingUi) -> Unit,
    onDismiss: (MeetingUi) -> Unit,
    modifier: Modifier = Modifier
) {
    // dismissed가 false인 회의만 필터링
    val availableMeetings = meetings.filter { !it.dismissed }

    // 리스트가 비어있으면 표시하지 않음
    if (availableMeetings.isEmpty()) return

    // 첫 번째 회의 표시
    val meeting = availableMeetings.first()
    var timeUntilText by remember { mutableStateOf(calculateTimeUntil(meeting.scheduledStartTime)) }

    // 매 분마다 시간 업데이트
    LaunchedEffect(meeting.scheduledStartTime) {
        while (true) {
            timeUntilText = calculateTimeUntil(meeting.scheduledStartTime)
            delay(60000L) // 1분마다 업데이트
        }
    }

    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp)
        ) {
            Row {
                Text(
                    text = "예정된 회의",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 22.sp,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = timeUntilText,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = green300,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }


            Spacer(modifier = Modifier.height(8.dp))

            // 회의 제목
            Text(
                text = meeting.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = gray200,
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 회의 시간
            Text(
                text = meeting.formattedTime,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                color = gray200,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                SmallSillokButton(
                    text = "상세 보기",
                    backgroundColor = green300,
                    onClick = { onJoinMeeting(meeting) }
                )

                Spacer(modifier = Modifier.width(12.dp))

                SmallSillokButton(
                    text = "숨기기",
                    backgroundColor = Color.White,
                    textColor = green200,
                    onClick = { onDismiss(meeting) },
                    borderColor = green300
                )
            }
        }
    }
}