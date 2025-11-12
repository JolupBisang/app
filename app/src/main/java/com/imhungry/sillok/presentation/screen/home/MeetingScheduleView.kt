package com.imhungry.sillok.presentation.screen.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imhungry.sillok.presentation.state.home.MeetingUi
import com.imhungry.sillok.ui.theme.green300
import com.imhungry.sillok.ui.theme.lightMeetingOutline
import com.imhungry.sillok.ui.theme.meetingOutline
import com.imhungry.sillok.ui.theme.primaryTextColor
import com.imhungry.sillok.ui.theme.tertiary
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MeetingScheduleView(
    onMeetingItemClick: (MeetingUi) -> Unit = {},
    onMonthChanged: (Int, Int) -> Unit = { _, _ -> }, // year, month
    meetings: List<MeetingUi> = emptyList(),
    onRefresh: () -> Unit = {},
    isLoading: Boolean = false,
    modifier: Modifier = Modifier,
) {
    var isCalendarView by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        Column {
            CalendarView(
                selectedDate = selectedDate,
                onDateSelected = { date ->
                    selectedDate = date
                },
                onTodayClick = {
                    selectedDate = LocalDate.now()
                },
                onMonthChanged = { yearMonth ->
                    selectedDate = null // 달이 변경되면 선택된 날짜 초기화
                    onMonthChanged(yearMonth.year, yearMonth.monthValue)
                },
                meetings = meetings,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            SelectedDateMeetingList(
                selectedDate = selectedDate,
                meetings = meetings,
                onMeetingItemClick = onMeetingItemClick,
                onRefresh = onRefresh,
                isLoading = isLoading,
                modifier = Modifier.fillMaxSize()
            )
        }

//        if (isCalendarView) {
//            Column {
//                CalendarView(
//                    selectedDate = selectedDate,
//                    onDateSelected = { date ->
//                        selectedDate = date
//                    },
//                    onTodayClick = {
//                        selectedDate = LocalDate.now()
//                    },
//                    scheduledMeetings = scheduledMeetings,
//                    pastMeetings = pastMeetings,
//                    modifier = Modifier.fillMaxWidth()
//                )
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                SelectedDateMeetingList(
//                    selectedDate = selectedDate,
//                    scheduledMeetings = scheduledMeetings,
//                    pastMeetings = pastMeetings,
//                    onMeetingItemClick = onMeetingItemClick,
//                    modifier = Modifier.fillMaxSize()
//                )
//            }
//        } else {
//            ListView(
//                onMeetingItemClick = onMeetingItemClick,
//                modifier = Modifier.fillMaxWidth(),
//                scheduledMeetings = scheduledMeetings,
//                pastMeetings = pastMeetings
//            )
//        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun SelectedDateMeetingList(
    selectedDate: LocalDate?,
    meetings: List<MeetingUi>,
    onMeetingItemClick: (MeetingUi) -> Unit,
    onRefresh: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isLoading,
        onRefresh = onRefresh
    )

    Box(
        modifier = modifier
            .pullRefresh(pullRefreshState)
    ) {
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val selectedDateString = selectedDate?.format(dateFormatter) ?: ""
        
        val meetingsOnSelectedDate = if (selectedDate != null) {
            meetings.filter { meeting ->
                meeting.scheduledStartTime.startsWith(selectedDateString)
            }
        } else {
            emptyList()
        }

        // 항상 LazyColumn을 사용하여 스크롤 가능하게 만듦
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (selectedDate == null || meetingsOnSelectedDate.isEmpty()) {
                // 날짜가 선택되지 않았거나 회의가 없을 때
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxSize()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "예정된 회의가 없습니다!",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                // 회의가 있을 때
                items(meetingsOnSelectedDate) { meeting ->
                        val backgroundColor: Color
                        val borderColor: Color
                        val borderWith: Dp
                        val titleColor: Color
                        val timeColor: Color

                        when (meeting.status) {
                            "IN_PROGRESS" -> {
                                // 진행 중인 회의
                                backgroundColor = Color.White
                                borderColor = green300
                                borderWith = 2.dp
                                titleColor = primaryTextColor
                                timeColor = primaryTextColor
                            }

                            "WAITING" -> {
                                // 예정된 회의
                                backgroundColor = Color.White
                                borderColor = meetingOutline
                                borderWith = 1.dp
                                titleColor = primaryTextColor
                                timeColor = tertiary
                            }

                            else -> {
                                // 그 외 (COMPLETED 등)
                                backgroundColor = Color.Transparent
                                borderColor = lightMeetingOutline
                                borderWith = 1.dp
                                titleColor = tertiary
                                timeColor = tertiary
                            }
                        }

                    MeetingListItem(
                        meeting = meeting,
                        onClick = { onMeetingItemClick(meeting) },
                        backgroundColor = backgroundColor,
                        borderColor = borderColor,
                        borderWith = borderWith,
                        titleColor = titleColor,
                        timeColor = timeColor
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MeetingListItem(
    meeting: MeetingUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    borderColor: Color = meetingOutline,
    borderWith: Dp = 1.dp,
    titleColor: Color = primaryTextColor,
    timeColor: Color = primaryTextColor,
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(borderWith, borderColor),
        shape = MaterialTheme.shapes.small
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = meeting.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = titleColor
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = meeting.timeRange,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Light,
                    color = timeColor
                )
            }
        }
    }
}