package com.imhungry.sillok.presentation.screen.meetingminutes.pager

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imhungry.sillok.R
import com.imhungry.sillok.presentation.screen.meeting.component.ConversationSummaryBar
import com.imhungry.sillok.presentation.screen.meeting.component.SummaryListItem
import com.imhungry.sillok.presentation.screen.meetingminutes.components.MeetingTabRow
import com.imhungry.sillok.presentation.util.DateTimeUtils
import com.imhungry.sillok.presentation.viewmodel.meetingminutes.MeetingMinutesViewModel
import com.imhungry.sillok.ui.components.Divider
import com.imhungry.sillok.ui.components.HighlightText

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MeetingMinutesSummaryScreen(
    selectedTab: Int,
    onTabClick: (Int) -> Unit,
    onTimeClick: (Long) -> Unit,
    meetingMinutesViewModel: MeetingMinutesViewModel
) {
    val state by meetingMinutesViewModel.state.collectAsState()
    val summaries = state.summaries
    val recapSummary = state.recapSummary
    val participationRates = state.participationRates
    var isExpanded by rememberSaveable { mutableStateOf(true) }
    var isExpanded2 by rememberSaveable { mutableStateOf(true) }
    var isExpanded3 by rememberSaveable { mutableStateOf(true) }
    var isExpanded4 by rememberSaveable { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        MeetingTabRow(selectedTab = selectedTab, onTabClick = onTabClick)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
        ) {
            item {
                SectionWithToggle(
                    title = "진행 시간",
                    onToggle = { isExpanded = !isExpanded },
                    content = {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = state.actualStartTime,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(Modifier.width(15.dp))
                                Text(
                                    text = "~",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(Modifier.width(15.dp))
                                Text(
                                    text = state.actualEndTime,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(Modifier.width(32.dp))
                                Text(
                                    text = "${state.actualDurationMinutes}분",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    },
                    expanded = isExpanded
                )
            }

            item {
                SectionWithToggle(
                    title = "대화 점유율",
                    onToggle = { isExpanded2 = !isExpanded2 },
                    content = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            if (participationRates.isNotEmpty()) {
                                ConversationSummaryBar(participationRates = participationRates)
                            }
                        }
                    },
                    expanded = isExpanded2
                )
            }

            item {
                SectionWithToggle(
                    title = "전체 요약",
                    onToggle = { isExpanded3 = !isExpanded3 },
                    content = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Text(
                                text = recapSummary,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    },
                    expanded = isExpanded3
                )
            }
            item {
                SectionWithToggle(
                    title = "중간 요약",
                    onToggle = { isExpanded4 = !isExpanded4 },
                    showDivider = false,
                    content = {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            summaries.forEachIndexed { index, summary ->
                                SummaryListItem(
                                    summary = summary,
                                    onClick = {
                                        // 중간 요약의 timestamp를 밀리초로 변환하여 오디오 재생 위치로 이동
                                        val seekMillis = DateTimeUtils.timeStringToMillis(summary.timestamp)
                                        if (seekMillis != null) {
                                            onTimeClick(seekMillis.coerceAtLeast(0L))
                                        }
                                    }
                                )
                                Spacer(Modifier.height(16.dp))
                                if (index == summaries.lastIndex) {
                                    Spacer(Modifier.height(28.dp))
                                }
                            }
                        }
                    },
                    expanded = isExpanded4
                )
            }
        }
    }
}

@Composable
fun SectionWithToggle(
    title: String,
    onToggle: () -> Unit,
    content: @Composable () -> Unit,
    expanded: Boolean,
    showDivider: Boolean = true
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onToggle
                )
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            HighlightText(
                text = title
            )
            Image(
                painter = painterResource(id = R.drawable.expand2),
                contentDescription = "접기",
                modifier = Modifier
                    .size(24.dp)
                    .rotate(if (expanded) 90f else 0f)
            )

        }

        AnimatedVisibility(visible = expanded) {
            content()
        }
        if (showDivider) {
            Divider()
        }
    }
}


