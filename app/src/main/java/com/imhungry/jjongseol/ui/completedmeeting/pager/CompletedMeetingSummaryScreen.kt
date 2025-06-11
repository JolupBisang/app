package com.imhungry.jjongseol.ui.completedmeeting.pager

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imhungry.jjongseol.R
import com.imhungry.jjongseol.data.model.meeting.response.MeetingDetailRes
import com.imhungry.jjongseol.data.model.participationrate.dto.ParticipationRateDto
import com.imhungry.jjongseol.data.model.participationrate.response.ParticipationRateHistoryRes
import com.imhungry.jjongseol.data.model.participationrate.response.ParticipationRateHistoryRes.UserParticipationRate
import com.imhungry.jjongseol.data.model.summary.dto.SummaryDto
import com.imhungry.jjongseol.data.model.summary.response.SummaryListRes
import com.imhungry.jjongseol.ui.completedmeeting.component.MeetingTabRow
import com.imhungry.jjongseol.ui.component.summary.ConversationSummaryBar
import com.imhungry.jjongseol.ui.component.summary.SummaryListItem
import com.imhungry.jjongseol.ui.theme.Pretend
import com.imhungry.jjongseol.ui.theme.primaryBackground
import com.imhungry.jjongseol.ui.theme.primaryButton
import com.imhungry.jjongseol.ui.theme.tertiary
import com.imhungry.jjongseol.util.DateTimeUtils
import com.imhungry.jjongseol.viewmodel.SummaryViewModel

@Composable
fun CompletedMeetingSummaryScreen(
    summarys: List<SummaryListRes>,
    fullSummary: List<SummaryListRes>,
    startMillis: Long,
    selectedTab: Int,
    onTabClick: (Int) -> Unit,
    onTimeClick: (Long) -> Unit,
    meetingDetail: MeetingDetailRes,
    userParticipationRates: List<UserParticipationRate>,
    summaryViewModel: SummaryViewModel,
    meetingId: Long
) {
    var isExpanded by rememberSaveable { mutableStateOf(true) }
    var isExpanded2 by rememberSaveable { mutableStateOf(true) }
    var isExpanded3 by rememberSaveable { mutableStateOf(true) }
    var isExpanded4 by rememberSaveable { mutableStateOf(true) }
    val sortedRates = userParticipationRates.sortedByDescending { it.rate }
    val rateList = sortedRates.map { it.rate }
    val nicknameList = sortedRates.map { it.nickname }
    val isLoading by summaryViewModel.isLoading.collectAsState()
    Column(modifier = Modifier
        .fillMaxWidth()
        .background(primaryBackground)
        .padding(WindowInsets.statusBars.asPaddingValues())
        .padding(horizontal = 20.dp)
    ) {
        MeetingTabRow(selectedTab = selectedTab, onTabClick = onTabClick)
        Divider()
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
        ) {
            item {
                SectionWithToggle(
                    title = "진행 시간",
                    isExpanded = isExpanded,
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
                                Text(text = "(실제시작)", style = MaterialTheme.typography.bodyLarge,
                                    fontFamily = Pretend, fontWeight = FontWeight.Medium)
                                Spacer(Modifier.width(15.dp))
                                Text(text = "~", style = MaterialTheme.typography.bodyLarge,
                                    fontFamily = Pretend, fontWeight = FontWeight.Medium)
                                Spacer(Modifier.width(15.dp))
                                Text(text = "(실제종료시각)", style = MaterialTheme.typography.bodyLarge,
                                    fontFamily = Pretend, fontWeight = FontWeight.Medium)
                                Spacer(Modifier.width(32.dp))
                                Text(text = "(실제진행시간)", style = MaterialTheme.typography.bodyLarge,
                                    fontFamily = Pretend, fontWeight = FontWeight.Medium)
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = DateTimeUtils.localIsoToTimeString(meetingDetail.scheduledStartTime),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = Pretend, fontWeight = FontWeight.Medium)
                                Spacer(Modifier.width(15.dp))
                                Text(text = "~", style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = Pretend, fontWeight = FontWeight.Medium)
                                Spacer(Modifier.width(15.dp))
                                Text(text = DateTimeUtils.localIsoToTimeStringPlusMinutes(meetingDetail.scheduledStartTime, meetingDetail.targetTime),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = Pretend, fontWeight = FontWeight.Medium)
                                Spacer(Modifier.width(32.dp))
                                Text(text = "${meetingDetail.targetTime}분", style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = Pretend, fontWeight = FontWeight.Medium)
                            }
                        }
                    },
                    expanded = isExpanded
                )
            }

            item {
                SectionWithToggle(
                    title = "대화 점유율",
                    isExpanded = isExpanded2,
                    onToggle = { isExpanded2 = !isExpanded2 },
                    content = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            ConversationSummaryBar(
                                participantData = rateList,
                                participantNames = nicknameList
                            )
                        }
                    },
                    expanded = isExpanded2
                )
            }

            item {
                SectionWithToggle(
                    title = "전체 요약",
                    isExpanded = isExpanded3,
                    onToggle = { isExpanded3 = !isExpanded3 },
                    content = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            fullSummary.getOrNull(0)?.let {
                                Text(
                                    text = it.content,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = Pretend, fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    },
                    expanded = isExpanded3
                )
            }
            item {
                SectionWithToggle(
                    title = "중간 요약",
                    isExpanded = isExpanded4,
                    onToggle = { isExpanded4 = !isExpanded4 },
                    showDivider = false,
                    content = {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            itemsIndexed(summarys) { index, summary ->
                                val elapsedMillis = DateTimeUtils.isoToMillis(summary.timestamp) - startMillis
                                val elapsed = DateTimeUtils.getElapsedString(startMillis, summary.timestamp)
                                SummaryListItem(
                                    summary = summary.content,
                                    timeText = elapsed,
                                    onClick = { onTimeClick(elapsedMillis.coerceAtLeast(0L)) }
                                )
                                Spacer(Modifier.height(16.dp))
                                if (index == summarys.lastIndex) {
                                    Spacer(Modifier.height(28.dp))
                                }
                                if (
                                    index == summarys.lastIndex &&
                                    !isLoading
                                ) {
                                    LaunchedEffect(key1 = summarys.size) {
                                        summaryViewModel.loadSummaries(
                                            meetingId = meetingId,
                                            isRecap = true,
                                            reset = false
                                        )
                                    }
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
    isExpanded: Boolean,
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
            Box(
                modifier = Modifier
                    .drawBehind {
                        val underlineHeight = 7.dp.toPx()
                        drawRect(
                            color = Color(0x40186848),
                            topLeft = Offset(0f, size.height - underlineHeight),
                            size = androidx.compose.ui.geometry.Size(size.width, underlineHeight)
                        )
                    }
            ) {
                Text(
                    text = title,
                    fontFamily = Pretend,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 15.sp
                )
            }
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


