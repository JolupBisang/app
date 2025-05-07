package com.imhungry.jjongseol.ui.newmeeting.dateandtime

import android.widget.NumberPicker
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import java.util.*

@Composable
fun TimeDurationPicker(
    onStartTimeChanged: (String) -> Unit,
    onEndTimeChanged: (String) -> Unit,
    onDurationChanged: (Int) -> Unit
) {
    var startTime by remember { mutableStateOf("HH:MM") }
    var endTime by remember { mutableStateOf("HH:MM") }
    var durationInMinutes by remember { mutableStateOf("") }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    LaunchedEffect(startTime) {
        if (startTime.matches(Regex("\\d{2}:\\d{2}"))) {
            onStartTimeChanged(startTime)
            durationInMinutes.toIntOrNull()?.let { duration ->
                calculateEndTime(startTime, duration) {
                    endTime = it
                    onEndTimeChanged(it)
                }
            }
        }
    }

    LaunchedEffect(endTime) {
        if (endTime.matches(Regex("\\d{2}:\\d{2}"))) {
            onEndTimeChanged(endTime)
            if (startTime.matches(Regex("\\d{2}:\\d{2}"))) {
                updateDurationFromTimes(startTime, endTime) {
                    durationInMinutes = it.toString()
                    onDurationChanged(it)
                }
            }
        }
    }

    LaunchedEffect(durationInMinutes) {
        if (durationInMinutes.isNotEmpty()) {
            durationInMinutes.toIntOrNull()?.let { duration ->
                onDurationChanged(duration)
                if (startTime.matches(Regex("\\d{2}:\\d{2}"))) {
                    calculateEndTime(startTime, duration) {
                        endTime = it
                        onEndTimeChanged(it)
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier=Modifier.weight(10f)) {
                TimePickerButton("시작 시간", startTime) {
                    showStartTimePicker = true
                }
            }
            Spacer(Modifier.weight(1f))
            Text("~", style = MaterialTheme.typography.h6)
            Spacer(Modifier.weight(1f))
            Box(modifier=Modifier.weight(10f)) {
                TimePickerButton("종료 시간", endTime) {
                    showEndTimePicker = true
                }
            }
            Spacer(Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically, modifier=Modifier.weight(10f)){
                Box(modifier=Modifier.weight(4f)) {
                    DurationInput(durationInMinutes, onDurationChange = { newValue ->
                        durationInMinutes = newValue
                    })
                }
                Spacer(Modifier.width(10.dp))
                Text("분", style = TextStyle(fontSize = 15.sp), modifier=Modifier.weight(1f))
            }
        }

        if (showStartTimePicker) {
            CustomTimePickerDialog(
                initialTime = startTime,
                onConfirm = { selectedTime ->
                    startTime = selectedTime
                    showStartTimePicker = false
                },
                onDismiss = {
                    showStartTimePicker = false
                }
            )
        }

        if (showEndTimePicker) {
            CustomTimePickerDialog(
                initialTime = endTime,
                onConfirm = { selectedTime ->
                    endTime = selectedTime
                    showEndTimePicker = false
                },
                onDismiss = {
                    showEndTimePicker = false
                }
            )
        }
    }
}

fun calculateEndTime(start: String, duration: Int, onResult: (String) -> Unit) {
    val (hour, minute) = start.split(":").map { it.toInt() }
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        add(Calendar.MINUTE, duration)
    }
    onResult(String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)))
}

fun updateDurationFromTimes(start: String, end: String, onResult: (Int) -> Unit) {
    val startCalendar = Calendar.getInstance().apply {
        val (hour, minute) = start.split(":").map { it.toInt() }
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
    }

    val endCalendar = Calendar.getInstance().apply {
        val (hour, minute) = end.split(":").map { it.toInt() }
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
    }

    if (endCalendar.before(startCalendar)) {
        endCalendar.add(Calendar.DATE, 1)  //다음 날로 설정하기
    }

    val duration = ((endCalendar.timeInMillis - startCalendar.timeInMillis) / 60000).toInt()
    onResult(duration)
}


@Composable
fun TimePickerButton(label: String, time: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(60.dp)
            .fillMaxWidth()
            .widthIn(min = 100.dp),
        shape = RoundedCornerShape(15.dp),
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
        border = BorderStroke(1.dp, Color.Gray)
    ) {
        Text(time, fontSize = 16.sp)
    }
}

@Composable
fun DurationInput(value: String, modifier: Modifier = Modifier, onDurationChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = {
            try {
                var numericValue = it.toInt()
                if (numericValue > 1440) {
                    numericValue = 1440
                }
                onDurationChange(numericValue.toString())
            } catch (e: NumberFormatException) {
                if (it.isBlank()) {
                    onDurationChange("0")
                }
            }
        },
        label = { Text("") },
        singleLine = true,
        modifier = modifier
            .height(60.dp)
            .border(1.dp, Color.Gray, RoundedCornerShape(15.dp))
            .fillMaxWidth(),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            textColor = Color.Black,
            cursorColor = Color.Black,
            backgroundColor = Color.White,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent
        ),
        textStyle = TextStyle(
            fontSize = 15.sp,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
    )
}


@Composable
fun CustomTimePickerDialog(
    initialTime: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val validInitialTime = if (initialTime.matches(Regex("\\d{2}:\\d{2}"))) initialTime else "12:00"
    var selectedHour by remember { mutableStateOf(validInitialTime.split(":").first().toInt()) }
    var selectedMinute by remember { mutableStateOf(validInitialTime.split(":").last().toInt()) }
    var showDialog by remember { mutableStateOf(true) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                onDismiss()},
            confirmButton = {
                TextButton(onClick = {
                    val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                    onConfirm(formattedTime)
                    showDialog = false
                }) {
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                    onDismiss() }
                )
                {
                    Text("취소")
                }
            },
            text = {
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center) {
                    AndroidView(
                        factory = { context ->
                            NumberPicker(context).apply {
                                minValue = 0
                                maxValue = 23
                                value = selectedHour
                                setOnValueChangedListener { _, _, newVal ->
                                    selectedHour = newVal
                                }
                            }
                        }
                    )
                    Spacer(Modifier.width(20.dp))
                    AndroidView(
                        factory = { context ->
                            NumberPicker(context).apply {
                                minValue = 0
                                maxValue = 59
                                value = selectedMinute
                                setOnValueChangedListener { _, _, newVal ->
                                    selectedMinute = newVal
                                }
                            }
                        }
                    )
                }
            }
        )
    }
}
