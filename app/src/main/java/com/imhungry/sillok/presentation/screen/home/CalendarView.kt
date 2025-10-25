package com.imhungry.sillok.presentation.screen.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.imhungry.sillok.R
import com.imhungry.sillok.domain.model.meeting.MeetingDetailSummary
import com.imhungry.sillok.ui.components.Divider
import com.imhungry.sillok.ui.components.SillokTextButton
import com.imhungry.sillok.ui.theme.gray300
import com.imhungry.sillok.ui.theme.lightSurface
import com.imhungry.sillok.ui.theme.primarySurface
import com.imhungry.sillok.ui.theme.primaryTextColor
import com.imhungry.sillok.ui.theme.tertiary
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarView(
    selectedDate: LocalDate = LocalDate.now(),
    onDateSelected: (LocalDate) -> Unit = {},
    onTodayClick: () -> Unit = {},
    scheduledMeetings: List<MeetingDetailSummary> = emptyList(),
    pastMeetings: List<MeetingDetailSummary> = emptyList(),
    modifier: Modifier = Modifier
) {
    var currentMonth by remember { mutableStateOf(YearMonth.from(selectedDate)) }

    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = modifier.fillMaxWidth()
        ) {
            CalendarNavigationBar(
                currentMonth = currentMonth,
                onPreviousMonth = {
                    currentMonth = currentMonth.minusMonths(1)
                },
                onNextMonth = {
                    currentMonth = currentMonth.plusMonths(1)
                },
                onTodayClick = {
                    currentMonth = YearMonth.from(LocalDate.now())
                    onTodayClick()
                }
            )

            Spacer(modifier = Modifier.height(28.dp))

            CalendarWeekHeader()

            Spacer(modifier = Modifier.height(8.dp))

            CalendarDateGrid(
                currentMonth = currentMonth,
                selectedDate = selectedDate,
                scheduledMeetings = scheduledMeetings,
                pastMeetings = pastMeetings,
                onDateSelected = onDateSelected
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 318.dp)
                .align(Alignment.TopCenter)
        ) {
            Divider()
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun CalendarNavigationBar(
    currentMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onTodayClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 42.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.prev),
                contentDescription = "이전 달",
                modifier = Modifier
                    .size(18.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onPreviousMonth() }
            )

            Text(
                text = "${currentMonth.year} ${currentMonth.month.getDisplayName(TextStyle.SHORT, Locale.KOREAN)}",
                style = MaterialTheme.typography.titleMedium,
            )

            Image(
                painter = painterResource(id = R.drawable.next),
                contentDescription = "다음 달",
                modifier = Modifier
                    .size(18.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onNextMonth() }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 6.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            SillokTextButton(
                text = "오늘",
                onClick = onTodayClick,
                textColor = gray300
            )
        }
    }
}

@Composable
private fun CalendarWeekHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 42.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        val weekDays = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")
        
        weekDays.forEach { day ->
            Text(
                text = day,
                style = MaterialTheme.typography.labelSmall,
                color = tertiary,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun CalendarDateGrid(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    scheduledMeetings: List<MeetingDetailSummary>,
    pastMeetings: List<MeetingDetailSummary>,
    onDateSelected: (LocalDate) -> Unit
) {
    val firstDayOfMonth = currentMonth.atDay(1)
    val lastDayOfMonth = currentMonth.atEndOfMonth()

    val calendarDays = mutableListOf<LocalDate?>()

    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // 0: 일요일, 1: 월요일, ...
    for (i in firstDayOfWeek downTo 1) {
        calendarDays.add(firstDayOfMonth.minusDays(i.toLong()))
    }

    for (day in 1..lastDayOfMonth.dayOfMonth) {
        calendarDays.add(currentMonth.atDay(day))
    }

    val remainingDays = 7 - (calendarDays.size % 7)
    if (remainingDays < 7) {
        for (day in 1..remainingDays) {
            calendarDays.add(currentMonth.plusMonths(1).atDay(day))
        }
    }

    val rows = calendarDays.chunked(7)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 42.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        rows.forEach { weekDays ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                weekDays.forEach { date ->
                    if (date != null) {
                        val hasMeetings = hasMeetingsOnDate(date, scheduledMeetings, pastMeetings)
                        val isCurrentMonth = date.month == currentMonth.month

						CalendarDateItem(
							date = date,
							isCurrentMonth = isCurrentMonth,
							isSelected = date == selectedDate,
							hasMeetings = hasMeetings && isCurrentMonth,
							onClick = {
                                if (isCurrentMonth) {
                                    onDateSelected(date)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun hasMeetingsOnDate(
    date: LocalDate,
    scheduledMeetings: List<MeetingDetailSummary>,
    pastMeetings: List<MeetingDetailSummary>
): Boolean {
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val dateString = date.format(dateFormatter)
    
    return (scheduledMeetings + pastMeetings).any { meeting ->
        meeting.scheduledStartTime.startsWith(dateString)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun CalendarDateItem(
    date: LocalDate,
    isCurrentMonth: Boolean,
    isSelected: Boolean,
    hasMeetings: Boolean,
    onClick: () -> Unit
) {
    val textColor = when {
        isSelected -> primarySurface
        hasMeetings -> primarySurface
        isCurrentMonth -> primaryTextColor
        else -> Color.Transparent
    }

    val backgroundColor = if (isSelected) {
        lightSurface
    } else {
        Color.Transparent
    }

    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .then(
                if (isCurrentMonth) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}