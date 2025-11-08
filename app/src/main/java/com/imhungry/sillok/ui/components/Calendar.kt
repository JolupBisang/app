package com.imhungry.sillok.ui.components

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
import com.imhungry.sillok.ui.theme.lightSurface
import com.imhungry.sillok.ui.theme.primarySurface
import com.imhungry.sillok.ui.theme.primaryTextColor
import com.imhungry.sillok.ui.theme.tertiary
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Calendar(
    selectedDate: LocalDate? = null,
    onDateSelected: (LocalDate) -> Unit = {},
    onDateCleared: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var currentMonth by remember {
        mutableStateOf(selectedDate?.let { YearMonth.from(it) } ?: YearMonth.from(LocalDate.now()))
    }
    var previousMonth by remember { mutableStateOf(currentMonth) }

    // 달이 변경될 때 선택 초기화
    LaunchedEffect(currentMonth) {
        if (currentMonth != previousMonth) {
            onDateCleared()
            previousMonth = currentMonth
        }
    }

    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = modifier.fillMaxWidth()
        ) {
            // 상단 네비게이션 바
            CalendarNavigationBar(
                currentMonth = currentMonth,
                onPreviousMonth = {
                    currentMonth = currentMonth.minusMonths(1)
                },
                onNextMonth = {
                    currentMonth = currentMonth.plusMonths(1)
                }
            )

            Spacer(modifier = Modifier.height(28.dp))

            // 요일 헤더
            CalendarWeekHeader()

            Spacer(modifier = Modifier.height(8.dp))

            // 날짜 그리드
            CalendarDateGrid(
                currentMonth = currentMonth,
                selectedDate = selectedDate,
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
    onNextMonth: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 42.dp, end = 42.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 이전 달 버튼
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

            // 현재 년월 표시
            Text(
                text = "${currentMonth.year} ${
                    currentMonth.month.getDisplayName(
                        TextStyle.SHORT,
                        Locale.KOREAN
                    )
                }",
                style = MaterialTheme.typography.titleMedium,
            )

            // 다음 달 버튼
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
    }
}

@Composable
private fun CalendarWeekHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
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
    onDateSelected: (LocalDate) -> Unit
) {
    val firstDayOfMonth = currentMonth.atDay(1)
    val lastDayOfMonth = currentMonth.atEndOfMonth()

    // 달력에 표시할 날짜들을 계산 (이전 달의 마지막 주부터 다음 달의 첫 주까지)
    val calendarDays = mutableListOf<LocalDate?>()

    // 이전 달의 마지막 주 날짜들 추가
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // 0: 일요일, 1: 월요일, ...
    for (i in firstDayOfWeek downTo 1) {
        calendarDays.add(firstDayOfMonth.minusDays(i.toLong()))
    }

    // 현재 달의 날짜들 추가
    for (day in 1..lastDayOfMonth.dayOfMonth) {
        calendarDays.add(currentMonth.atDay(day))
    }

    // 다음 달의 첫 주 날짜들 추가 (7의 배수가 될 때까지)
    val remainingDays = 7 - (calendarDays.size % 7)
    if (remainingDays < 7) {
        for (day in 1..remainingDays) {
            calendarDays.add(currentMonth.plusMonths(1).atDay(day))
        }
    }

    // 7일씩 행으로 나누기
    val rows = calendarDays.chunked(7)

    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        rows.forEach { weekDays ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                weekDays.forEach { date ->
                    if (date != null) {
                        val isCurrentMonth = date.month == currentMonth.month

                        CalendarDateItem(
                            date = date,
                            isCurrentMonth = isCurrentMonth,
                            isSelected = date == selectedDate,
                            onClick = {
                                // 현재 달에 포함된 날짜만 클릭 가능
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
@Composable
private fun CalendarDateItem(
    date: LocalDate,
    isCurrentMonth: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val textColor = when {
        isSelected -> primarySurface
        isCurrentMonth -> primaryTextColor
        else -> Color.Transparent
    }

    val backgroundColor = when {
        isSelected -> lightSurface
        else -> Color.Transparent
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