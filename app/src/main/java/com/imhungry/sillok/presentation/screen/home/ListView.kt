package com.imhungry.sillok.presentation.screen.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.imhungry.sillok.R
import com.imhungry.sillok.domain.model.meeting.MeetingDetailSummary
import com.imhungry.sillok.ui.components.Divider
import com.imhungry.sillok.ui.components.SillokTextButton
import com.imhungry.sillok.ui.theme.tertiary

@Composable
fun ListView(
    onMeetingItemClick: (MeetingDetailSummary) -> Unit = {},
    modifier: Modifier = Modifier,
    scheduledMeetings: List<MeetingDetailSummary> = emptyList(),
    pastMeetings: List<MeetingDetailSummary> = emptyList()
) {
    // 섹션 확장 상태
    var isScheduledExpanded by remember { mutableStateOf(true) }
    var isPastExpanded by remember { mutableStateOf(true) }
    
    LazyColumn(
        modifier = modifier.fillMaxWidth()
    ) {
        // 예정된 회의 섹션
        item {
            MeetingSection(
                title = "예정된 회의",
                meetings = scheduledMeetings,
                isExpanded = isScheduledExpanded,
                onToggle = { isScheduledExpanded = !isScheduledExpanded },
                onMeetingItemClick = onMeetingItemClick
            )
        }


        // 구분선
        item {
            Divider()
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 지난 회의 섹션
        item {
            MeetingSection(
                title = "지난 회의",
                meetings = pastMeetings,
                isExpanded = isPastExpanded,
                onToggle = { isPastExpanded = !isPastExpanded },
                onMeetingItemClick = onMeetingItemClick
            )
        }
    }
}

@Composable
fun MeetingSection(
    title: String,
    meetings: List<MeetingDetailSummary>,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onMeetingItemClick: (MeetingDetailSummary) -> Unit
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
                ) { onToggle() }
            ,
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
                            displayedCount = if (displayedCount + 8 <= meetings.size) displayedCount + 8 else meetings.size
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
    meeting: MeetingDetailSummary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
        ,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 회의명
        SillokTextButton(
            text = "∘  ${meeting.title}",
            onClick = onClick
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // 날짜
        val date = meeting.scheduledStartTime.split("T", " ")[0].replace("-", ".")
        Text(
            modifier = Modifier.width(80.dp),
            text = date,
            style = MaterialTheme.typography.bodySmall,
            color = tertiary
        )
    }
}