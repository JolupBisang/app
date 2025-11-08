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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun SelectedDateMeetingList(
    selectedDate: LocalDate?,
    meetings: List<MeetingUi>,
    onMeetingItemClick: (MeetingUi) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(top = 8.dp)
    ) {
        if (selectedDate == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "예정된 회의가 없습니다!",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp
                )
            }
        } else {
            val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val selectedDateString = selectedDate.format(dateFormatter)
            
            val meetingsOnSelectedDate = meetings.filter { meeting ->
                meeting.scheduledStartTime.startsWith(selectedDateString)
            }
            
            // WAITING이나 IN_PROGRESS 상태인 회의가 있는지 확인
            val hasActiveMeetings = meetingsOnSelectedDate.any { meeting ->
                meeting.status == "WAITING" || meeting.status == "IN_PROGRESS"
            }

            if (meetingsOnSelectedDate.isEmpty()) {
                // 회의가 아예 없을 때
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "예정된 회의가 없습니다!",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Normal
                    )
                }
            } else {
                // 회의가 있을 때
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
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

//                    // WAITING이나 IN_PROGRESS 상태인 회의가 없을 때 메시지 표시
//                    if (!hasActiveMeetings) {
//                        item {
//                            Box(
//                                modifier = Modifier
//                                    .fillMaxSize()
//                                    .background(Color.Yellow)
//                                    .padding(vertical = 16.dp),
//                                contentAlignment = Alignment.Center
//                            ) {
//                                Text(
//                                    text = "예정된 회의가 없습니다!",
//                                    style = MaterialTheme.typography.bodyMedium,
//                                    fontWeight = FontWeight.Normal
//                                )
//                            }
//                        }
//                    }
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