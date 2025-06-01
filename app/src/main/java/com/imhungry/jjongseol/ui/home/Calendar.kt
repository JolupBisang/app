package com.imhungry.jjongseol.ui.home

import android.os.Build
import android.widget.NumberPicker
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.imhungry.jjongseol.R
import com.imhungry.jjongseol.data.model.home.MeetingResponse
import com.imhungry.jjongseol.ui.theme.UserPink
import com.imhungry.jjongseol.ui.theme.md_theme_button_color_blue
import com.imhungry.jjongseol.viewmodel.ScheduleViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun CalendarScreen(navController: NavController, viewModel: ScheduleViewModel = hiltViewModel()) {
    var currentYearMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showNumberPicker by remember { mutableStateOf(false) }
    val meetings by viewModel.meetings

    LaunchedEffect(currentYearMonth) {
        viewModel.loadMeetings(currentYearMonth)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp),
        horizontalArrangement = Arrangement.End,
    ) {
        Text(
            text = "오늘", color = Color.Black, modifier = Modifier
                .padding(end = 5.dp)
                .clickable {
                    currentYearMonth = YearMonth.now()
                    selectedDate = LocalDate.now()
                }
        )
    }
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 36.dp)) {
        CalendarHeader(
            yearMonth = currentYearMonth,
            onPrev = { currentYearMonth = currentYearMonth.minusMonths(1) },
            onNext = { currentYearMonth = currentYearMonth.plusMonths(1) },
            onTextClick = { showNumberPicker = true }
        )
        Box(modifier = Modifier.background(UserPink).padding(top = 15.dp, bottom = 15.dp, end = 2.dp)) {
            CalendarGrid(
                yearMonth = currentYearMonth,
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it },
                meetings = meetings
            )
        }
    }

    if (showNumberPicker) {
        NumberPickerDialog(
            initialYearMonth = currentYearMonth,
            onDismiss = { showNumberPicker = false },
            onConfirm = {
                currentYearMonth = it
                val newDay = selectedDate.dayOfMonth
                val maxDay = it.lengthOfMonth()
                selectedDate = it.atDay(minOf(newDay, maxDay))
                showNumberPicker = false
            }

        )
    }

    Divider(
        color = Color.Gray,
        thickness = 1.dp,
        modifier = Modifier.padding(top = 25.dp,bottom = 5.dp)
    )

    val handleScheduleClick = { schedule: MeetingResponse ->
        navController.navigate("meetingDetail/${schedule.id}")
    }

    val selectedDateStr = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

    DailyScheduleView(
        selectedDate = selectedDateStr,
        schedules = meetings,
        onScheduleClick = handleScheduleClick
    )

}

@Composable
fun CalendarHeader(
    yearMonth: YearMonth,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onTextClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${yearMonth.year} ${yearMonth.monthValue}월",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            ),
            color = Color.Black,
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onTextClick()
            }
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.prev),
                contentDescription = "previous month",
                modifier = Modifier
                    .size(24.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onPrev()
                    }
            )

            Image(
                painter = painterResource(R.drawable.next),
                contentDescription = "next month",
                modifier = Modifier
                    .size(24.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onNext()
                    }
            )
        }
    }
}



@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarGrid(
    yearMonth: YearMonth,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    meetings: List<MeetingResponse>
) {
    val today = LocalDate.now()
    val firstDayOfMonth = yearMonth.atDay(1)
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
    val totalCells = ((firstDayOfWeek + daysInMonth + 6) / 7) * 7

    val dates = (0 until totalCells).map { index ->
        val dayOffset = index - firstDayOfWeek
        firstDayOfMonth.plusDays(dayOffset.toLong())
    }

    val scheduleMap = meetings.groupBy {
        LocalDateTime.parse(it.scheduledStartTime).toLocalDate()
    }

    Column (modifier = Modifier.background(Color.White)){
        Row(Modifier.fillMaxWidth()) {
            listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT").forEach {
                Text(
                    text = it,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.padding(top = 2.dp))

        dates.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                week.forEach { date ->
                    val isCurrentMonth = date.month == yearMonth.month
                    val isSelected = date == selectedDate
                    val hasMeeting = scheduleMap.containsKey(date)
                    val meetingsOnDate = scheduleMap[date].orEmpty()

                    val hasCompleted = meetingsOnDate.any { it.status == "COMPLETED" }
                    val hasWaiting = meetingsOnDate.any { it.status == "WAITING" }
                    //진행중이거나 취소된 회의는 어떻게 표시할건지

                    val backgroundColor = if (isSelected) {
                        Color(0xFFDAF2FF)
                    } else {
                        Color.Transparent
                    }

                    //색상은 임시지정
                    val textColor = when {
                        //isSelected -> Color.Black
                        !date.isBefore(today) && hasMeeting -> Color.Yellow //미래 회의
                        date.isBefore(today) && hasCompleted -> md_theme_button_color_blue //완료된 회의
                        date.isBefore(today) && hasWaiting -> Color.Green //대기 중 회의
                        isCurrentMonth && date.dayOfWeek.value % 7 == 0 -> Color.Red //일요일
                        //isCurrentMonth && date.dayOfWeek.value % 7 == 6 -> Color.Blue //토요일
                        isCurrentMonth -> Color.Black //일반
                        else -> Color.White
                        //else -> Color.LightGray //다크 모드 하면 이걸로
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1.15f)
                            .padding(2.dp)
                            .background(backgroundColor, shape = RoundedCornerShape(100))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                if (isCurrentMonth) onDateSelected(date)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = date.dayOfMonth.toString(),
                            fontSize = 15.sp,
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun NumberPickerDialog(
    initialYearMonth: YearMonth,
    onDismiss: () -> Unit,
    onConfirm: (YearMonth) -> Unit
) {
    var selectedYear by remember { mutableStateOf(initialYearMonth.year) }
    var selectedMonth by remember { mutableStateOf(initialYearMonth.monthValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        confirmButton = {
            TextButton(onClick = {
                onConfirm(YearMonth.of(selectedYear, selectedMonth))
            }) {
                Text(text = "확인", color = Color(0xFF1E93EF))
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onConfirm(YearMonth.of(selectedYear, selectedMonth))
            }) {
                Text(text = "취소", color = Color(0xFF1E93EF))
            }},
        text = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                AndroidView(
                    factory = { context ->
                        NumberPicker(context).apply {
                            minValue = 1900
                            maxValue = 2125
                            value = selectedYear
                            setOnValueChangedListener { _, _, newVal ->
                                selectedYear = newVal
                            }
                        }
                    }
                )
                AndroidView(
                    factory = { context ->
                        NumberPicker(context).apply {
                            minValue = 1
                            maxValue = 12
                            value = selectedMonth
                            setOnValueChangedListener { _, _, newVal ->
                                selectedMonth = newVal
                            }
                        }
                    }
                )
            }
        }
    )
}


@Composable
fun DailyScheduleView(selectedDate: String, schedules: List<MeetingResponse>, onScheduleClick: (MeetingResponse) -> Unit) {
    val dailySchedules = schedules.filter { it.scheduledStartTime.startsWith(selectedDate) }

    if (dailySchedules.isEmpty()) {
        Text("일정이 없습니다", modifier = Modifier.fillMaxWidth().widthIn(100.dp).padding(top=20.dp), textAlign = TextAlign.Center)
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xEDF3E7))
                .padding(8.dp)
        ) {
            Text("일정 기록", fontSize = 18.sp, fontWeight = FontWeight.Bold )
            Spacer(Modifier.height(10.dp))
            dailySchedules.forEach { schedule ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 2.5.dp, horizontal = 10.dp)
                            .clickable { onScheduleClick(schedule) },
                ) {
                    Text(
                        text = "∘ "+schedule.title,
                        fontSize = 15.sp,

                    )
                    Spacer(Modifier.weight(1f))
                    val startTime = LocalDateTime.parse(schedule.scheduledStartTime)
                    Text(
                        text = startTime.toLocalTime()
                            .format(DateTimeFormatter.ofPattern("HH:mm")),
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
