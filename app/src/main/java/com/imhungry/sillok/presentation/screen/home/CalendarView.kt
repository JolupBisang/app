package com.imhungry.sillok.presentation.screen.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.NumberPicker
import androidx.compose.foundation.layout.Arrangement
import com.imhungry.sillok.R
import com.imhungry.sillok.presentation.state.home.MeetingUi
import com.imhungry.sillok.ui.components.MediumSillokButton
import com.imhungry.sillok.ui.components.SillokTextButton
import com.imhungry.sillok.ui.theme.cancledMeeting
import com.imhungry.sillok.ui.theme.completedMeeting
import com.imhungry.sillok.ui.theme.gray300
import com.imhungry.sillok.ui.theme.inProgressMeeting
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarView(
    selectedDate: LocalDate? = null,
    onDateSelected: (LocalDate) -> Unit = {},
    onTodayClick: () -> Unit = {},
    onMonthChanged: (YearMonth) -> Unit = {},
    meetings: List<MeetingUi> = emptyList(),
    modifier: Modifier = Modifier
) {
    var currentMonth by remember {
        mutableStateOf(selectedDate?.let { YearMonth.from(it) } ?: YearMonth.from(LocalDate.now()))
    }
    var isInitialized by remember { mutableStateOf(false) }
    var isDateInitialized by remember { mutableStateOf(false) }
    var showYearMonthPicker by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
            // 달이 변경되었을 때 해당 달에 오늘 날짜가 있으면 자동 선택
            val today = LocalDate.now()
            if (currentMonth.year == today.year && currentMonth.month == today.month) {
                onDateSelected(today)
            }
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
                },
                onYearMonthClick = {
                    showYearMonthPicker = true
                }
            )
            
            if (showYearMonthPicker) {
                YearMonthPickerBottomSheet(
                    currentYearMonth = currentMonth,
                    onYearMonthSelected = { yearMonth ->
                        currentMonth = yearMonth
                        showYearMonthPicker = false
                    },
                    onDismiss = {
                        showYearMonthPicker = false
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            CalendarWeekHeader()

            Spacer(modifier = Modifier.height(6.dp))

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
    onTodayClick: () -> Unit,
    onYearMonthClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 64.dp, end = 64.dp, top = 8.dp),
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
                text = "${currentMonth.year} ${
                    currentMonth.month.getDisplayName(
                        TextStyle.SHORT,
                        Locale.KOREAN
                    )
                }",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onYearMonthClick() }
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
            .padding(horizontal = 10.dp),
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
            .padding(horizontal = 8.dp)
    ) {
        rows.forEach { weekDays ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                weekDays.forEach { date ->
                    if (date != null) {
                        val meetingCounts = getMeetingCountsByStatus(date, meetings)
                        val isCurrentMonth = date.month == currentMonth.month
                        val isToday = date == LocalDate.now()

                        CalendarDateItem(
                            date = date,
                            isCurrentMonth = isCurrentMonth,
                            isSelected = selectedDate != null && date == selectedDate,
                            isToday = isToday,
                            meetingCounts = meetingCounts,
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
private fun getMeetingCountsByStatus(
    date: LocalDate,
    meetings: List<MeetingUi>
): Map<String, Int> {
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val dateString = date.format(dateFormatter)

    val meetingsOnDate = meetings.filter { meeting ->
        meeting.scheduledStartTime.startsWith(dateString)
    }

    if (meetingsOnDate.isEmpty()) {
        return emptyMap()
    }

    val counts = mutableMapOf<String, Int>()
    meetingsOnDate.forEach { meeting ->
        val status = meeting.status ?: "CANCELED"
        counts[status] = counts.getOrDefault(status, 0) + 1
    }

    return counts
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun CalendarDateItem(
    date: LocalDate,
    isCurrentMonth: Boolean,
    isSelected: Boolean,
    isToday: Boolean,
    meetingCounts: Map<String, Int>,
    onClick: () -> Unit
) {
    val textColor = when {
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
            .width(40.dp)
            .height(36.dp),
        contentAlignment = Alignment.TopCenter
    ) {
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
        if (isCurrentMonth && meetingCounts.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .padding(top = 28.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // 취소된 회의
                    repeat(meetingCounts.getOrDefault("CANCELED", 0)) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .background(cancledMeeting, shape = CircleShape)
                        )
                    }
                    // 완료된 회의
                    repeat(meetingCounts.getOrDefault("COMPLETED", 0)) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .background(completedMeeting, shape = CircleShape)
                        )
                    }
                    // 대기 중인 회의
                    repeat(meetingCounts.getOrDefault("WAITING", 0)) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .background(waitingMeeting, shape = CircleShape)
                        )
                    }
                    // 진행 중인 회의
                    repeat(meetingCounts.getOrDefault("IN_PROGRESS", 0)) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .background(inProgressMeeting, shape = CircleShape)
                        )
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun YearMonthPickerBottomSheet(
    currentYearMonth: YearMonth,
    onYearMonthSelected: (YearMonth) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedYear by remember { mutableStateOf(currentYearMonth.year) }
    var selectedMonth by remember { mutableStateOf(currentYearMonth.monthValue) }
    val currentYear = LocalDate.now().year
    val minYear = currentYear - 10
    val maxYear = currentYear + 10

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                // 년도 선택
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AndroidView(
                        factory = { ctx ->
                            NumberPicker(ctx).apply {
                                minValue = minYear
                                maxValue = maxYear
                                value = selectedYear
                                setOnValueChangedListener { _, _, newVal ->
                                    selectedYear = newVal
                                }
                            }
                        },
                        update = { picker ->
                            picker.value = selectedYear
                        },
                        //modifier = Modifier.height(200.dp)
                    )
                }

                Spacer(Modifier.width(16.dp))
                // 월 선택
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AndroidView(
                        factory = { ctx ->
                            NumberPicker(ctx).apply {
                                minValue = 1
                                maxValue = 12
                                value = selectedMonth
                                setOnValueChangedListener { _, _, newVal ->
                                    selectedMonth = newVal
                                }
                            }
                        },
                        update = { picker ->
                            picker.value = selectedMonth
                        },
                        //modifier = Modifier.height(200.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 확인 버튼
            MediumSillokButton(
                text = "확인",
                onClick = {
                    onYearMonthSelected(YearMonth.of(selectedYear, selectedMonth))
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}