package com.imhungry.jjongseol.ui.newmeeting.breaktime

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BreakTimeRow() {
    var breakTime by remember { mutableStateOf("") }
    var breakTimeMinute by remember { mutableStateOf("") }

    fun handleBreakTimeChange(newBreakTime: String) {
        if (newBreakTime.all { it.isDigit() }) {
            breakTime = newBreakTime
            if (breakTime.isNotEmpty() && breakTimeMinute.isNotEmpty()) {
                val breakTimeInt = breakTime.toIntOrNull() ?: 0
                val breakTimeMinuteInt = breakTimeMinute.toIntOrNull() ?: 0
                if (breakTimeInt <= breakTimeMinuteInt) {
                    breakTimeMinute = "0"
                }
            }
        }
    }

    fun handleBreakTimeMinuteChange(newBreakTimeMinute: String) {
        if (newBreakTimeMinute.all { it.isDigit() }) {
            val breakTimeInt = breakTime.toIntOrNull() ?: 0
            val newBreakTimeMinuteInt = newBreakTimeMinute.toIntOrNull() ?: 0
            if (breakTime.isNotEmpty() && newBreakTimeMinuteInt >= breakTimeInt) {
                breakTimeMinute = "0"
            } else {
                breakTimeMinute = newBreakTimeMinute
            }
        }
    }

    Row {
        Row(modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = breakTime,
                onValueChange = ::handleBreakTimeChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
                    .border(1.dp, Color.Gray, RoundedCornerShape(15.dp)),
                singleLine = true,
                textStyle = TextStyle(fontSize = 16.sp),
                placeholder = {
                    Text(
                        "",
                        style = TextStyle(
                            color = Color.LightGray
                        )
                    )
                },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Color.Black,
                    cursorColor = Color.Black,
                    backgroundColor = Color.White,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            Spacer(Modifier.width(2.dp))
            Text("분 마다", style = TextStyle(fontSize = 15.sp))
        }

        Row(modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically){
            Spacer(Modifier.width(20.dp))

            OutlinedTextField(
                value = breakTimeMinute,
                onValueChange = ::handleBreakTimeMinuteChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
                    .border(1.dp, Color.Gray, RoundedCornerShape(15.dp)),
                singleLine = true,
                textStyle = TextStyle(fontSize = 16.sp),
                placeholder = {
                    Text(
                        "",
                        style = TextStyle(
                            color = Color.LightGray
                        )
                    )
                },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Color.Black,
                    cursorColor = Color.Black,
                    backgroundColor = Color.White,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            Spacer(Modifier.width(2.dp))
            Text("분", style = TextStyle(fontSize = 15.sp))
        }
    }
}
