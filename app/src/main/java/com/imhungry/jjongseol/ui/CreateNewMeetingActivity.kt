package com.imhungry.jjongseol.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.imhungry.jjongseol.MainActivity
import java.util.Calendar


class CreateNewMeetingActivity : ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)

        setContent{
            CreateNewMeetingScreen()
        }
    }
}

@Composable
fun CreateNewMeetingScreen(){
    val context = LocalContext.current
    val meetingTitle = remember { mutableStateOf("") }
    val titleText = remember { mutableStateOf("") }
    val memberText = remember { mutableStateOf("") }

    ConstraintLayout (modifier = Modifier.background(Color.White)){
        val scrollList = createRef()

        LazyColumn(
            modifier = Modifier
                .padding(30.dp)
                .fillMaxSize()
                .constrainAs(scrollList) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end)
                    start.linkTo(parent.start)
                },
        ) {
            item{
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(bottom = 20.dp),
                    contentAlignment = Alignment.Center
                ){
                    Text("회의 생성",
                        style = TextStyle(
                            color = Color.Gray,
                            fontSize = 30.sp,
                        )
                    )
                }
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .padding(start = 8.dp, end = 8.dp),
                    contentAlignment = Alignment.CenterStart
                )
                {
                    Text("제목",
                        style = TextStyle(
                            color = Color.DarkGray,
                            fontSize = 15.sp,
                        )
                    )
                }

                OutlinedTextField(
                    value = titleText.value,
                    onValueChange = { titleText.value = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .border(1.dp, Color.Gray, RoundedCornerShape(15.dp)),
                    textStyle = TextStyle(fontSize = 16.sp),
                    placeholder = {
                        Text("{이름}님의 회의",
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
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .padding(start = 8.dp, end = 8.dp),
                    contentAlignment = Alignment.CenterStart
                )
                {
                    Text("참석자",
                        style = TextStyle(
                            color = Color.DarkGray,
                            fontSize = 15.sp,
                        )
                    )
                }

                OutlinedTextField(
                    value = memberText.value,
                    onValueChange = { memberText.value = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .border(1.dp, Color.Gray, RoundedCornerShape(15.dp)),
                    textStyle = TextStyle(fontSize = 16.sp),
                    placeholder = {
                        Text("이름, 이메일로 검색",
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
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .padding(start = 8.dp, end = 8.dp),
                    contentAlignment = Alignment.CenterStart
                )
                {
                    Text("일시",
                        style = TextStyle(
                            color = Color.DarkGray,
                            fontSize = 15.sp,
                        )
                    )
                }
                DatePickerField()
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .padding(start = 8.dp, end = 8.dp),
                    contentAlignment = Alignment.CenterStart
                )
                {
                    Text("시간",
                        style = TextStyle(
                            color = Color.DarkGray,
                            fontSize = 15.sp,
                        )
                    )
                }
                TimeDurationPicker()
            }

        }

        Box(
            contentAlignment = Alignment.TopStart,
            modifier = Modifier
                //.background(color = colorResource(R.color.black))
                .padding(25.dp)
        ) {
            CustomBackButton(onClick = {
                val intent = Intent(context, MainActivity::class.java)
                context.startActivity(intent)
            })
        }

    }
}

@Composable
fun CustomBackButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .height(50.dp)
            .width(50.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = Color.White
        ),
        border = BorderStroke(1.dp, Color.Gray),
        elevation = ButtonDefaults.elevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp,
            disabledElevation = 0.dp
        ),
        content = {
            Text("<",
                style = TextStyle(
                    color = Color.Black,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.W400
                )
            )
        }
    )
}

@Composable
fun DatePickerField() {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val dateText = remember { mutableStateOf("YYYY / MM / DD") }
    val textColor = if (dateText.value == "YYYY / MM / DD") Color.LightGray else Color.Black

    val datePickerDialog = DatePickerDialog(
        context,
        { _, selectedYear, selectedMonth, selectedDay ->
            dateText.value = "${selectedYear} / ${selectedMonth + 1} / $selectedDay"
        }, year, month, day
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .padding(8.dp)
            .border(1.dp, Color.Gray, RoundedCornerShape(15.dp))
            .clickable {
                datePickerDialog.show()
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = dateText.value,
            style = TextStyle(
                color = textColor,
                fontSize = 16.sp
            ),
            modifier = Modifier
                .padding(10.dp)
                .padding(start = 5.dp)
        )
    }
}

@Composable
fun TimeDurationPicker() {
    val context = LocalContext.current
    var startTime by remember { mutableStateOf("HH:MM") }
    var endTime by remember { mutableStateOf("HH:MM") }
    var durationInMinutes by remember { mutableStateOf("") }

    // 종료 시간 자동 계산
    fun calculateEndTime(start: String, duration: Int) {
        val (hour, minute) = start.split(":").map { it.toInt() }
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            add(Calendar.MINUTE, duration)
        }
        endTime = String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
    }

    // 시간 차이를 분으로 계산
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
        modifier = Modifier.fillMaxWidth().padding(5.dp),
        horizontalAlignment = Alignment.CenterHorizontally

    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            //Text("시작 시간:")
            Button(modifier = Modifier
                .width(80.dp)
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
            Spacer(Modifier.width(10.dp))
            Text(" ~ ",
                style = TextStyle(
                    fontSize = 20.sp
                ))
            Spacer(Modifier.width(10.dp))
            Button(modifier = Modifier
                .width(80.dp)
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
                    fontSize = 20.sp
                ))
        }

    }
}
