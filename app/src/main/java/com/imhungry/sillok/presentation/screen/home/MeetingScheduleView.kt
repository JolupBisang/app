package com.imhungry.sillok.presentation.screen.home

import android.R.attr.minHeight
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.SnackbarDefaults.backgroundColor
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imhungry.sillok.domain.model.meeting.MeetingDetailSummary
import com.imhungry.sillok.presentation.state.home.MeetingUi
import com.imhungry.sillok.ui.components.Divider
import com.imhungry.sillok.ui.components.SillokTextButton
import com.imhungry.sillok.ui.theme.border
import com.imhungry.sillok.ui.theme.green300
import com.imhungry.sillok.ui.theme.lightMeetingOutline
import com.imhungry.sillok.ui.theme.meetingOutline
import com.imhungry.sillok.ui.theme.primaryButton
import com.imhungry.sillok.ui.theme.primarySurface
import com.imhungry.sillok.ui.theme.primaryTextColor
import com.imhungry.sillok.ui.theme.tertiary
import com.imhungry.sillok.ui.theme.whiteBackground
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime

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

            Spacer(modifier = Modifier.height(16.dp))

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

            if (meetingsOnSelectedDate.isNotEmpty()) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
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
                }
            } else {
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
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun MeetingListItem(
    meeting: MeetingUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    borderColor: Color = meetingOutline,
    borderWith: Dp = 1.dp,
    titleColor: Color = primaryTextColor,
    timeColor: Color = primaryTextColor,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 42.dp),
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        border = BorderStroke(borderWith, borderColor),
        shape = MaterialTheme.shapes.small,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
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