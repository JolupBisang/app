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
import androidx.compose.runtime.LaunchedEffect
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
import com.imhungry.sillok.presentation.state.home.MeetingUi
import com.imhungry.sillok.ui.components.SillokTextButton
import com.imhungry.sillok.ui.theme.cancledMeeting
import com.imhungry.sillok.ui.theme.completedMeeting
import com.imhungry.sillok.ui.theme.gray300
import com.imhungry.sillok.ui.theme.green200
import com.imhungry.sillok.ui.theme.green300
import com.imhungry.sillok.ui.theme.inProgressMeeting
import com.imhungry.sillok.ui.theme.lightSurface
import com.imhungry.sillok.ui.theme.primarySurface
import com.imhungry.sillok.ui.theme.primaryTextColor
import com.imhungry.sillok.ui.theme.selectedDate
import com.imhungry.sillok.ui.theme.tertiary
import com.imhungry.sillok.ui.theme.waitingMeeting
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarView(
    selectedDate: LocalDate? = null,
    onDateSelected: (LocalDate) -> Unit = {},
    onTodayClick: () -> Unit = {},
    onMonthChanged: (YearMonth) -> Unit = {},
    meetings: List<MeetingUi> = emptyList(),
    modifier: Modifier = Modifier
) {
    var currentMonth by remember { mutableStateOf(selectedDate?.let { YearMonth.from(it) } ?: YearMonth.from(LocalDate.now())) }
    var isInitialized by remember { mutableStateOf(false) }
    var isDateInitialized by remember { mutableStateOf(false) }

    // 초기 로드 시 selectedDate가 null이면 오늘 날짜 선택
    LaunchedEffect(Unit) {
        if (!isDateInitialized && selectedDate == null) {
            isDateInitialized = true
            onDateSelected(LocalDate.now())
        }
    }

    // 달이 변경될 때마다 콜백 호출 (초기 로드 제외)
    LaunchedEffect(currentMonth) {
        if (isInitialized) {
            onMonthChanged(currentMonth)
        } else {
            isInitialized = true
        }
    }

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

            Spacer(modifier = Modifier.height(32.dp))

            CalendarWeekHeader()

            Spacer(modifier = Modifier.height(8.dp))

            CalendarDateGrid(
                currentMonth = currentMonth,
                selectedDate = selectedDate,
                meetings = meetings,
                onDateSelected = onDateSelected
            )
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
                .padding(start = 56.dp, end = 56.dp, top = 8.dp),
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
                textColor = gray300,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
private fun CalendarWeekHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
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
    selectedDate: LocalDate?,
    meetings: List<MeetingUi>,
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
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        rows.forEach { weekDays ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                weekDays.forEach { date ->
                    if (date != null) {
                        val meetingStatus = getMeetingStatusOnDate(date, meetings)
                        val isCurrentMonth = date.month == currentMonth.month
                        val isToday = date == LocalDate.now()

						CalendarDateItem(
							date = date,
							isCurrentMonth = isCurrentMonth,
							isSelected = selectedDate != null && date == selectedDate,
							isToday = isToday,
							meetingStatus = meetingStatus,
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
private fun getMeetingStatusOnDate(
    date: LocalDate,
    meetings: List<MeetingUi>
): String? {
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val dateString = date.format(dateFormatter)
    
    val meetingsOnDate = meetings.filter { meeting ->
        meeting.scheduledStartTime.startsWith(dateString)
    }
    
    if (meetingsOnDate.isEmpty()) {
        return null
    }
    
    // 우선순위: IN_PROGRESS > WAITING > COMPLETED > 그 외
    return when {
        meetingsOnDate.any { it.status == "IN_PROGRESS" } -> "IN_PROGRESS"
        meetingsOnDate.any { it.status == "WAITING" } -> "WAITING"
        meetingsOnDate.any { it.status == "COMPLETED" } -> "COMPLETED"
        else -> "CANCELED"
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun CalendarDateItem(
    date: LocalDate,
    isCurrentMonth: Boolean,
    isSelected: Boolean,
    isToday: Boolean,
    meetingStatus: String?,
    onClick: () -> Unit
) {
    val textColor = when {
        meetingStatus != null && isCurrentMonth -> {
            when (meetingStatus) {
                "IN_PROGRESS" -> inProgressMeeting
                "WAITING" -> waitingMeeting
                "COMPLETED" -> completedMeeting
                "CANCELED" -> cancledMeeting
                else -> primarySurface
            }
        }
        isCurrentMonth -> primaryTextColor
        else -> Color.Transparent
    }

    val backgroundColor = if (isSelected) {
        selectedDate
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