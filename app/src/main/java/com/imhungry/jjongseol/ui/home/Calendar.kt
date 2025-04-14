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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.imhungry.jjongseol.R
import com.imhungry.jjongseol.ui.home.meetingdata.ScheduleItem
import com.imhungry.jjongseol.ui.theme.md_theme_button_color_blue
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

val schedules = listOf(
    ScheduleItem("회의 1", "13:00", "2025.04.26"),
    ScheduleItem("회의 2", "15:00", "2025.04.26"),
    ScheduleItem("회의 1", "13:00", "2025.04.26"),
    ScheduleItem("회의 2", "15:00", "2025.04.26"),
    ScheduleItem("회의 1", "13:00", "2025.04.26"),
    ScheduleItem("회의 2", "15:00", "2025.04.26"),
    ScheduleItem("회의 1", "13:00", "2025.04.26"),
    ScheduleItem("회의 2", "15:00", "2025.04.26"),
    ScheduleItem("회의 1", "13:00", "2025.04.26"),
    ScheduleItem("회의 2", "15:00", "2025.04.26"),
    ScheduleItem("워크샵", "11:00", "2025.04.26"),
    ScheduleItem("고객 미팅", "14:00", "2025.04.27"),
    ScheduleItem("프로젝트 리뷰", "16:00", "2025.06.28"),
    ScheduleItem("웹 세미나", "10:00", "2025.07.01"),
    ScheduleItem("팀 런치", "12:00", "2025.07.01"),
    ScheduleItem("제품 발표회", "15:00", "2025.07.02"),
    ScheduleItem("경영진 회의", "09:00", "2025.07.03"),
    ScheduleItem("기술 트레이닝", "13:30", "2025.07.03"),
    ScheduleItem("마케팅 전략 논의", "11:00", "2025.07.04"),
    ScheduleItem("영업 팀 회의", "15:00", "2025.07.04"),
    ScheduleItem("개발자 컨퍼런스", "10:00", "2025.07.05"),
    ScheduleItem("재무 검토 회의", "14:00", "2025.07.05")
)

@Composable
fun CalendarScreen(navController: NavController) {
    var currentYearMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showNumberPicker by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp),
        horizontalArrangement = Arrangement.End,
    ) {
        Text(
            text = "오늘", color = md_theme_button_color_blue, modifier = Modifier
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
        CalendarGrid(
            yearMonth = currentYearMonth,
            selectedDate = selectedDate,
            onDateSelected = { selectedDate = it },
            schedules = schedules
        )
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
        modifier = Modifier.padding(top = 30.dp,bottom = 10.dp)
    )

    val handleScheduleClick = { schedule: ScheduleItem ->
        //navController.navigate("completeProfile")
    }

    DailyScheduleView(selectedDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd")),
        schedules, handleScheduleClick)

}

@Composable
fun CalendarHeader(
    yearMonth: YearMonth,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onTextClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, top = 16.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = "${yearMonth.year}년 ${yearMonth.monthValue}월",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Black,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    onTextClick()
                }
        )
        Spacer(Modifier.weight(1f))
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
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarGrid(
    yearMonth: YearMonth,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    schedules: List<ScheduleItem>
) {
    val firstDayOfMonth = yearMonth.atDay(1)
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
    val totalCells = ((firstDayOfWeek + daysInMonth + 6) / 7) * 7

    val dates = (0 until totalCells).map { index ->
        val dayOffset = index - firstDayOfWeek
        val date = firstDayOfMonth.plusDays(dayOffset.toLong())
        date
    }

    val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
    val datesWithEvents = schedules
        .filter { schedule ->
            val scheduleDate = LocalDate.parse(schedule.date, formatter)
            scheduleDate.month == yearMonth.month && scheduleDate.year == yearMonth.year
        }
        .map { LocalDate.parse(it.date, formatter) }
        .toSet()

    Column {
        Row(Modifier.fillMaxWidth()) {
            listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT").forEach {
                Text(
                    text = it,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = Color.LightGray,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.padding(top = 4.dp))

        dates.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                week.forEach { date ->
                    val isCurrentMonth = date.month == yearMonth.month
                    val isSelected = date == selectedDate
                    val dayOfWeek = date.dayOfWeek.value % 7
                    val hasEvent = date in datesWithEvents

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .background(
                                when {
                                    isSelected && isCurrentMonth && hasEvent -> Color(0xFFDAF2FF) //일정이 있고 선택된 날
                                    isSelected && isCurrentMonth -> Color(0xFFDAF2FF) //선택된 날
                                    !isCurrentMonth -> Color.White.copy(alpha = 0.3f)
                                    else -> Color.Transparent
                                },
                                shape = RoundedCornerShape(100)
                            )
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
                            style = MaterialTheme.typography.bodyMedium,
                            color = when {
                                isSelected && hasEvent -> md_theme_button_color_blue //일정이 있고 선택된 날
                                hasEvent -> md_theme_button_color_blue //일정만 있는 날
                                isCurrentMonth && dayOfWeek == 0 -> Color.Red //일요일
                                isCurrentMonth -> Color.Black //일반
                                else -> Color.White
                            }
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
fun DailyScheduleView(selectedDate: String, schedules: List<ScheduleItem>, onScheduleClick: (ScheduleItem) -> Unit) {
    val dailySchedules = schedules.filter { it.date == selectedDate }

    if (dailySchedules.isEmpty()) {
        Text("일정이 없습니다", modifier = Modifier.fillMaxWidth().widthIn(100.dp).padding(top=20.dp), textAlign = TextAlign.Center)
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xEDF3E7))
                .padding(16.dp)
        ) {
            Text("일정 기록", style = androidx.compose.material.MaterialTheme.typography.h6)
            Spacer(Modifier.height(20.dp))
            dailySchedules.forEach { schedule ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                            .clickable { onScheduleClick(schedule) },
                ) {
                    Text(
                        text = schedule.title,
                        style = androidx.compose.material.MaterialTheme.typography.body1
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = schedule.time,
                        style = androidx.compose.material.MaterialTheme.typography.body1
                    )
                }
            }
        }
    }
}
