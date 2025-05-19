package com.imhungry.jjongseol.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun DataPickerCalendar(navController: NavController, initialSelectedDate: LocalDate, onDateSelected: (LocalDate, Boolean) -> Unit) {
    var currentYearMonth by remember { mutableStateOf(YearMonth.from(initialSelectedDate)) }
    var selectedDate by remember { mutableStateOf(initialSelectedDate) }
    var showNumberPicker by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.height(400.dp).width(500.dp),
        color = Color.White,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CalendarHeader(
                yearMonth = currentYearMonth,
                onPrev = { currentYearMonth = currentYearMonth.minusMonths(1) },
                onNext = { currentYearMonth = currentYearMonth.plusMonths(1) },
                onTextClick = { showNumberPicker = !showNumberPicker }
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (!showNumberPicker) {
                CalendarGrid(
                    yearMonth = currentYearMonth,
                    selectedDate = selectedDate,
                    onDateSelected = { selectedDate = it },
                    meetings = listOf()
                )
            } else {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = { onDateSelected(selectedDate, false) }) {
                    Text("취소")
                }
                TextButton(onClick = { onDateSelected(selectedDate, true) }) {
                    Text("확인")
                }
            }
        }
    }
}
