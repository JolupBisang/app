package com.imhungry.jjongseol.ui.component

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.imhungry.jjongseol.R
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun CalendarScreen() {
    var currentYearMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showNumberPicker by remember { mutableStateOf(false) }

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
            onDateSelected = { selectedDate = it }
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
    onDateSelected: (LocalDate) -> Unit
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

    Column {
        Row(Modifier.fillMaxWidth()) {
            listOf("일", "월", "화", "수", "목", "금", "토").forEach {
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

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .background(
                                when {
                                    isSelected && isCurrentMonth -> Color(0xFFDAF2FF)
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
                                isSelected -> Color.Blue
                                isCurrentMonth && dayOfWeek == 0 -> Color.Red
                                isCurrentMonth -> Color.Black
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
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(onClick = {
                    onConfirm(YearMonth.of(selectedYear, selectedMonth))
                }) {
                    Text(text = "취소", color = Color(0xFF1E93EF))
                }
                Spacer(modifier = Modifier.width(40.dp))
                TextButton(onClick = {
                    onConfirm(YearMonth.of(selectedYear, selectedMonth))
                }) {
                    Text(text = "확인", color = Color(0xFF1E93EF))
                }
            }
        },
        dismissButton = {},
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

