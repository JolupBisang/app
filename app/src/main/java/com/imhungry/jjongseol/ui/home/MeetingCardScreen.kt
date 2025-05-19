package com.imhungry.jjongseol.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import com.imhungry.jjongseol.data.model.home.MeetingResponse
import com.imhungry.jjongseol.ui.theme.md_theme_button_color_blue
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun MeetingCardList(meetings: List<MeetingResponse>, onJoinMeeting: (MeetingResponse) -> Unit) {
    val inProgressMeetings = remember(meetings) {
        meetings.filter { it.status == "IN_PROGRESS" }
    }
    var currentIndex by remember { mutableStateOf(0) }

    if (currentIndex < inProgressMeetings.size) {
        MeetingCard(
            meeting = inProgressMeetings[currentIndex],
            onDismiss = { currentIndex++ },
            onJoin = { onJoinMeeting(inProgressMeetings[currentIndex]) }
        )
    }
}

@Composable
fun MeetingCard(
    meeting: MeetingResponse,
    onDismiss: () -> Unit,
    onJoin: () -> Unit
) {
    val dateTime = LocalDateTime.parse(meeting.scheduledStartTime)
    val timeText = "${dateTime.toLocalDate()} ${dateTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))}"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .height(180.dp),
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "현재 진행 중인 회의가 있어요!", color = Color.DarkGray)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = meeting.title, color = Color.DarkGray)
            Text(text = "$timeText ~", color = Color.DarkGray)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "참여하지 않기",
                    modifier = Modifier
                        .clickable { onDismiss() }
                        .padding(10.dp),
                    color = md_theme_button_color_blue
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "바로 참여하기 >",
                    modifier = Modifier
                        .clickable { onJoin() }
                        .padding(10.dp),
                    color = md_theme_button_color_blue
                )
            }
        }
    }
}
