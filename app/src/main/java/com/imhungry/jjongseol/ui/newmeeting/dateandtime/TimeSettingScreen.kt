package com.imhungry.jjongseol.ui.newmeeting.dateandtime

import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldDefaults
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
import java.util.Calendar


@Composable
fun TimeDurationPicker() {
    val context = LocalContext.current
    var startTime by remember { mutableStateOf("HH:MM") }
    var endTime by remember { mutableStateOf("HH:MM") }
    var durationInMinutes by remember { mutableStateOf("") }

    fun calculateEndTime(start: String, duration: Int) {
        val (hour, minute) = start.split(":").map { it.toInt() }
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            add(Calendar.MINUTE, duration)
        }
        endTime = String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(
            Calendar.MINUTE))
    }

    fun calculateDuration(startTime: String, endTime: String) {
        if (startTime.isNotEmpty() && endTime.isNotEmpty()) {
            val start = Calendar.getInstance().apply {
                val (startHour, startMinute) = startTime.split(":").map { it.toInt() }
                set(Calendar.HOUR_OF_DAY, startHour)
                set(Calendar.MINUTE, startMinute)
            }
            val end = Calendar.getInstance().apply {
                val (endHour, endMinute) = endTime.split(":").map { it.toInt() }
                set(Calendar.HOUR_OF_DAY, endHour)
                set(Calendar.MINUTE, endMinute)
            }
            if (end.before(start)) {
                end.add(Calendar.DATE, 1)
            }
            val duration = (end.timeInMillis - start.timeInMillis) / (60 * 1000)
            durationInMinutes = duration.toString()
        }
    }

    fun pickTime(isStartTime: Boolean, updateTime: (String) -> Unit) {
        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            val formattedTime = String.format("%02d:%02d", hour, minute)
            updateTime(formattedTime)
            if (isStartTime) {
                startTime = formattedTime
            } else {
                endTime = formattedTime
            }
            if (startTime.isNotEmpty() && endTime.isNotEmpty() && startTime != "HH:MM" && endTime != "HH:MM") {
                calculateDuration(startTime, endTime)
            }
        }

        val currentTime = Calendar.getInstance()
        val (hour, minute) = when {
            isStartTime && startTime.matches(Regex("\\d{2}:\\d{2}")) -> startTime.split(":").map { it.toInt() }
            !isStartTime && endTime.matches(Regex("\\d{2}:\\d{2}")) -> endTime.split(":").map { it.toInt() }
            else -> listOf(currentTime.get(Calendar.HOUR_OF_DAY), currentTime.get(Calendar.MINUTE))
        }

        TimePickerDialog(context, timeSetListener, hour, minute, true).show()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally

    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(modifier = Modifier
                .height(60.dp),
                onClick = { pickTime(true, { startTime = it }) },
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.White
                ),
                border = BorderStroke(1.dp, Color.Gray)
            ) {
                Text(text = startTime)
            }
            Spacer(Modifier.width(5.dp))
            Text(" ~ ",
                style = TextStyle(
                    fontSize = 20.sp
                )
            )
            Spacer(Modifier.width(5.dp))
            Button(modifier = Modifier
                .height(60.dp),
                onClick = { pickTime(false, { endTime = it }) },
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.White
                ),
                border = BorderStroke(1.dp, Color.Gray),
            ) {
                Text(text = endTime)
            }
            Spacer(Modifier.width(10.dp))

            Spacer(Modifier.width(10.dp))
            OutlinedTextField(
                value = durationInMinutes,
                onValueChange = { newValue ->
                    val filtered = newValue.filter { it.isDigit() }
                    if (filtered.isNotEmpty()) {
                        val minutes = filtered.toInt()
                        if (minutes <= 14400) {
                            durationInMinutes = filtered
                        } else {
                            durationInMinutes = "14400"
                        }
                        if (startTime.matches(Regex("\\d{2}:\\d{2}"))) {
                            calculateEndTime(startTime, durationInMinutes.toInt())
                        }
                    } else {
                        durationInMinutes = ""
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .width(80.dp)
                    .height(60.dp)
                    .border(1.dp, Color.Gray, RoundedCornerShape(15.dp)),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Color.Black,
                    cursorColor = Color.Black,
                    backgroundColor = Color.White,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )
            Spacer(Modifier.width(10.dp))
            Text("분",
                style = TextStyle(
                    fontSize = 15.sp
                )
            )
        }

    }
}