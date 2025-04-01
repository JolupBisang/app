package com.imhungry.jjongseol.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import java.util.Calendar
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavController
import java.util.UUID

@Composable
fun CreateNewMeetingScreen(navController: NavController){
    //val context = LocalContext.current
    val meetingTitle = remember { mutableStateOf("") }
    val leaderName = remember { mutableStateOf("") }
    val memberLists = remember { mutableStateOf("") }
    val place = remember { mutableStateOf("") }

    ConstraintLayout (modifier = Modifier
        .background(Color.White)
        .fillMaxSize()){
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
                    height = Dimension.fillToConstraints
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
                Row() {
                    Column(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .height(30.dp)
                                .padding(start = 8.dp, end = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        )
                        {
                            Text(
                                "제목",
                                style = TextStyle(
                                    color = Color.DarkGray,
                                    fontSize = 15.sp,
                                )
                            )
                        }

                        OutlinedTextField(
                            value = meetingTitle.value,
                            onValueChange = { meetingTitle.value = it },
                            modifier = Modifier
                                .padding(8.dp)
                                .border(1.dp, Color.Gray, RoundedCornerShape(15.dp)),
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 16.sp),
                            placeholder = {
                                Text(
                                    "{이름}님의 회의",
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
                    Column(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .height(30.dp)
                                .padding(start = 8.dp, end = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        )
                        {
                            Text(
                                "리더",
                                style = TextStyle(
                                    color = Color.DarkGray,
                                    fontSize = 15.sp,
                                )
                            )
                        }

                        OutlinedTextField(
                            value = leaderName.value,
                            onValueChange = { leaderName.value = it },
                            modifier = Modifier
                                .padding(8.dp)
                                .border(1.dp, Color.Gray, RoundedCornerShape(15.dp)),
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 16.sp),
                            placeholder = {
                                Text(
                                    "{이름}",
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
                    Text("참석자",
                        style = TextStyle(
                            color = Color.DarkGray,
                            fontSize = 15.sp,
                        )
                    )
                }

                OutlinedTextField(
                    value = memberLists.value,
                    onValueChange = { memberLists.value = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .border(1.dp, Color.Gray, RoundedCornerShape(15.dp)),
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 16.sp),
                    placeholder = {
                        Text("이름, 이메일, 팀으로 검색",
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

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .padding(start = 8.dp, end = 8.dp),
                    contentAlignment = Alignment.CenterStart
                )
                {
                    Text("장소",
                        style = TextStyle(
                            color = Color.DarkGray,
                            fontSize = 15.sp,
                        )
                    )
                }

                OutlinedTextField(
                    value = place.value,
                    onValueChange = { place.value = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .border(1.dp, Color.Gray, RoundedCornerShape(15.dp)),
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 16.sp),
                    placeholder = {
                        Text("장소",
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
                    Text("아젠다",
                        style = TextStyle(
                            color = Color.DarkGray,
                            fontSize = 15.sp,
                        )
                    )
                }

                AgendaListScreen()
            }

            item{
                Column(modifier = Modifier) {
                    Box(
                        modifier = Modifier
                            .height(30.dp)
                            .padding(start = 8.dp, end = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    )
                    {
                        Text(
                            "쉬는 시간",
                            style = TextStyle(
                                color = Color.DarkGray,
                                fontSize = 15.sp,
                            )
                        )
                    }

                    BreakTimeRow()
                }

            }



        }


        Box(
            contentAlignment = Alignment.TopStart,
            modifier = Modifier
                .padding(
                    start = 25.dp,
                    top = 40.dp)
        ) {
            CustomBackButton(onClick = { navController.popBackStack() })
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

    fun calculateEndTime(start: String, duration: Int) {
        val (hour, minute) = start.split(":").map { it.toInt() }
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            add(Calendar.MINUTE, duration)
        }
        endTime = String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
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
                ))
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
                ))
        }

    }
}

data class AgendaItem(val id: String = UUID.randomUUID().toString(), var text: String, var isPlaceholder: Boolean = true)

@Composable
fun AgendaListScreen() {
    val itemList = remember { mutableStateListOf<AgendaItem>() }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(8.dp)
            .border(1.dp, Color.Gray, RoundedCornerShape(15.dp))
        ) {
            items(items = itemList, key = { it.id }) { item ->
                ListItemWithCircle(
                    item = item,
                    onEdit = { newText ->
                        item.text = newText
                        item.isPlaceholder = false
                    },
                    onDelete = { itemList.remove(item) }
                )
            }
        }

        Button(modifier = Modifier.fillMaxWidth().padding(
            start = 8.dp, end = 8.dp, top = 3.dp, bottom = 8.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.LightGray),
            onClick = {
                itemList.add(AgendaItem(text = "새 아젠다", isPlaceholder = true))
            }) {
            Text("+", style = TextStyle(color = Color.Blue, fontSize = 25.sp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListItemWithCircle(item: AgendaItem, onEdit: (String) -> Unit, onDelete: () -> Unit) {
    var editText by remember { mutableStateOf(item.text) }
    var editing by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            //고민(ExperimentalFoundationApi::class) <-- 오쪼지
            .combinedClickable(
                onClick = { },
                onLongClick = {
                    editing = true
                    if (item.isPlaceholder) {
                        editText = ""
                    }
                }
            )

    ) {
        Box(modifier = Modifier
            .padding(start = 15.dp)
            .size(10.dp)
            .background(color = Color.LightGray, shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(10.dp))

        if (editing) {
            TextField(
                value = editText,
                onValueChange = { editText = it },
                modifier = Modifier
                    .weight(1f)
                    .widthIn(max = 200.dp),
                onDone = {
                    editing = false
                    //고민
                    /*if (editText.isEmpty()) {
                        editText = item.text
                    }*/
                    onEdit(editText)
                },
                textStyle = TextStyle(fontSize = 15.sp, color = if (item.isPlaceholder) Color.Black else Color.Black),
                placeholder = {
                    if (item.isPlaceholder) Text(item.text, style = TextStyle(color = Color.Gray))
                }
            )
        } else {
            Text(
                text = editText,
                style = TextStyle(fontSize = 15.sp, color = if (item.isPlaceholder) Color.Gray else Color.Black),
                modifier = Modifier.weight(1f)
            )
        }

        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Edit",
            modifier = Modifier
                .clickable {
                    editing = true
                    if (item.isPlaceholder) {
                        editText = ""
                    }
                }
                .padding(8.dp)
        )
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete",
            modifier = Modifier
                .clickable { onDelete() }
                .padding(8.dp)
        )
    }
}

@Composable
fun TextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onDone: () -> Unit,
    textStyle: TextStyle,
    placeholder: @Composable (() -> Unit)? = null
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = {
            onDone()
            keyboardController?.hide()
        }),
        textStyle = textStyle,
        placeholder = placeholder
    )
}

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
