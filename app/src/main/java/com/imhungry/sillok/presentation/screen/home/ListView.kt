package com.imhungry.sillok.presentation.screen.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.imhungry.sillok.R
import com.imhungry.sillok.presentation.state.home.MeetingUi
import com.imhungry.sillok.ui.components.SillokTextButton
import com.imhungry.sillok.ui.theme.tertiary

@Composable
fun MeetingSection(
    title: String,
    meetings: List<MeetingUi>,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onMeetingItemClick: (MeetingUi) -> Unit
) {
    // 처음에 보여줄 항목 수
    var displayedCount by remember { mutableStateOf(8) }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { onToggle() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )

            Spacer(modifier = Modifier.weight(1f))

            Image(
                painter = painterResource(id = R.drawable.arrow_drop_up),
                contentDescription = if (isExpanded) "접기" else "펼치기",
                modifier = Modifier
                    .size(16.dp)
                    .rotate(if (isExpanded) 0f else 90f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isExpanded) {
            if (meetings.isEmpty()) {
                Text(
                    text = "회의가 없습니다.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = tertiary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 16.dp, bottom = 16.dp)
                )
            } else {
                // 표시할 회의 목록
                val displayedMeetings = meetings.take(displayedCount)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    displayedMeetings.forEach { meeting ->
                        MeetingItem(
                            meeting = meeting,
                            onClick = { onMeetingItemClick(meeting) }
                        )
                    }
                }

                if (displayedCount < meetings.size) {
                    Spacer(modifier = Modifier.height(12.dp))

                    SillokTextButton(
                        text = "더보기",
                        onClick = {
                            // 8개씩 더 표시
                            displayedCount =
                                if (displayedCount + 8 <= meetings.size) displayedCount + 8 else meetings.size
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 30.dp),
                        textColor = tertiary
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

            }
        }
    }
}

@Composable
fun MeetingItem(
    meeting: MeetingUi,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 회의명
        SillokTextButton(
            text = "∘  ${meeting.title}",
            onClick = onClick
        )

        Spacer(modifier = Modifier.weight(1f))

        // 날짜
        Text(
            modifier = Modifier.width(80.dp),
            text = meeting.timeRange,
            style = MaterialTheme.typography.bodySmall,
            color = tertiary
        )
    }
}