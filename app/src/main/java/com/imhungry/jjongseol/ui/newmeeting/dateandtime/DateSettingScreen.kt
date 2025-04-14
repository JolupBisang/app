package com.imhungry.jjongseol.ui.newmeeting.dateandtime

import android.widget.NumberPicker
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Icon
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.imhungry.jjongseol.ui.theme.md_theme_button_color_blue
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun DatePickerDialog() {
    val context = LocalContext.current
    val initialYearMonthDay = remember { LocalDate.now() }
    var selectedYear by remember { mutableStateOf(initialYearMonthDay.year) }
    var selectedMonth by remember { mutableStateOf(initialYearMonthDay.monthValue) }
    var selectedDay by remember { mutableStateOf(initialYearMonthDay.dayOfMonth) }
    var daysInMonth = remember(selectedYear, selectedMonth) {
        YearMonth.of(selectedYear, selectedMonth).lengthOfMonth()
    }

    val showDatePickerDialog = remember { mutableStateOf(false) }
    val dateText = remember { mutableStateOf("YYYY / MM / DD") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .height(55.dp)
            .border(1.dp, Color.Gray, RoundedCornerShape(15.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { showDatePickerDialog.value = true },
        horizontalArrangement  = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = dateText.value,
            style = TextStyle(fontSize = 16.sp, color = Color.Black),
            modifier = Modifier.padding(start = 20.dp, top = 10.dp, bottom = 10.dp)
        )
        Spacer(Modifier.weight(1f))

        Icon(
            imageVector = Icons.Filled.DateRange,
            contentDescription = "회의 날짜 선택",
            modifier = Modifier.padding(end = 10.dp)
        )
    }

    if (showDatePickerDialog.value) {
        AlertDialog(
            onDismissRequest = { showDatePickerDialog.value = false },
            confirmButton = {
                TextButton(onClick = {
                    dateText.value = "$selectedYear / $selectedMonth / $selectedDay"
                    showDatePickerDialog.value = false
                }) {
                    Text(text = "확인", color = md_theme_button_color_blue,
                        modifier = Modifier.padding(10.dp))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog.value = false }) {
                    Text(text = "취소", color = md_theme_button_color_blue,
                        modifier = Modifier.padding(10.dp))
                }
            },
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    NumberPickerComponent("", 1900, 2125, selectedYear) { newVal ->
                        selectedYear = newVal
                        daysInMonth = YearMonth.of(selectedYear, selectedMonth).lengthOfMonth()
                        selectedDay = minOf(selectedDay, daysInMonth)
                    }
                    NumberPickerComponent("", 1, 12, selectedMonth) { newVal ->
                        selectedMonth = newVal
                        daysInMonth = YearMonth.of(selectedYear, selectedMonth).lengthOfMonth()
                        selectedDay = minOf(selectedDay, daysInMonth)
                    }
                    NumberPickerComponent("", 1, daysInMonth, selectedDay) { newVal ->
                        selectedDay = newVal
                    }
                }
            }
        )
    }
}

@Composable
fun NumberPickerComponent(label: String, min: Int, max: Int, value: Int, onValueChange: (Int) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label)
        AndroidView(
            factory = { context ->
                NumberPicker(context).apply {
                    minValue = min
                    maxValue = max
                    setValue(value)
                    setOnValueChangedListener { _, _, newVal ->
                        onValueChange(newVal)
                    }
                }
            },
            update = { it.maxValue = max; it.minValue = min; it.value = value }
        )
    }
}
