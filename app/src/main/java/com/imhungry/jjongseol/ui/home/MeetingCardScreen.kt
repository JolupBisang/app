package com.imhungry.jjongseol.ui.home

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imhungry.jjongseol.data.model.home.MeetingResponse
import com.imhungry.jjongseol.service.MeetingSseService
import com.imhungry.jjongseol.ui.theme.SkyBlue
import com.imhungry.jjongseol.ui.theme.UserGreen1
import com.imhungry.jjongseol.ui.theme.UserGreen2
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun MeetingCardList(meetings: List<MeetingResponse>,
                    onJoinMeeting: (MeetingResponse) -> Unit,
                    isRunning: Boolean,
                    meetingId: Long) {
    val inProgressMeetings = remember(meetings) {
        meetings.filter { it.status == "IN_PROGRESS" }
    }

    var currentIndex by remember { mutableStateOf(0) }

    val prioritizedMeeting = remember(meetingId, isRunning, inProgressMeetings) {
        if (isRunning && meetingId != -1L) {
            inProgressMeetings.find { it.id == meetingId }
        } else null
    }

    if (prioritizedMeeting != null) {
        MeetingCard(
            meeting = prioritizedMeeting,
            isConnectedMeeting = true,
            onDismiss = { currentIndex++ },
            onJoin = { onJoinMeeting(prioritizedMeeting) }
        )
    } else if (currentIndex < inProgressMeetings.size) {
        MeetingCard(
            meeting = inProgressMeetings[currentIndex],
            isConnectedMeeting = false,
            onDismiss = { currentIndex++ },
            onJoin = { onJoinMeeting(inProgressMeetings[currentIndex]) }
        )
    }
}

@Composable
fun MeetingCard(
    meeting: MeetingResponse,
    isConnectedMeeting: Boolean,
    onDismiss: () -> Unit,
    onJoin: () -> Unit
) {
    val context = LocalContext.current
    val dateTime = LocalDateTime.parse(meeting.scheduledStartTime)
    val endTime = dateTime.plusMinutes(meeting.targetTime.toLong())
    val timeText = "${dateTime.toLocalDate()} ${dateTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))} ~ ${endTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))}"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .height(150.dp),
        elevation = 5.dp,
        shape = RoundedCornerShape(10.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(UserGreen2)
                .padding(20.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "📍 현재 진행 중인 회의가 있습니다", color = Color.Black, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = meeting.title, color = Color.DarkGray, fontSize = 13.sp)
            Text(text = timeText, color = Color.DarkGray, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isConnectedMeeting) "회의 나가기" else "참여하지 않기",
                        modifier = Modifier
                            .clickable {
                                if (isConnectedMeeting) {
                                    context.stopService(Intent(context, MeetingSseService::class.java))
                                }
                                onDismiss() }
                            .padding(vertical = 8.dp, horizontal = 25.dp),
                        color = Color.DarkGray,
                        fontSize = 15.sp
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = if (isConnectedMeeting) "다시 참여하기" else "바로 참여하기",
                        modifier = Modifier
                            .clickable { onJoin() }
                            .padding(vertical = 8.dp, horizontal = 25.dp),
                        color = UserGreen1,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
