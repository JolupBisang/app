package com.imhungry.jjongseol.ui.newmeeting

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import android.util.Log
import com.imhungry.jjongseol.data.model.meeting.MeetingReq
import com.imhungry.jjongseol.ui.home.DataPickerCalendar
import com.imhungry.jjongseol.ui.newmeeting.agenda.AgendaListScreen
import com.imhungry.jjongseol.ui.newmeeting.breaktime.BreakTimeRow
import com.imhungry.jjongseol.ui.newmeeting.dateandtime.TimeDurationPicker
import com.imhungry.jjongseol.ui.newmeeting.invite.SearchScreen
import com.imhungry.jjongseol.ui.theme.md_theme_button_color_blue
import com.imhungry.jjongseol.viewmodel.MeetingViewModel
import com.imhungry.jjongseol.viewmodel.UserViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun CreateNewMeetingScreen(navController: NavController){
    val meetingViewModel: MeetingViewModel = hiltViewModel()
    val userViewModel: UserViewModel = hiltViewModel()

    val meetingTitle = remember { mutableStateOf("") }
    val leaderName = remember { mutableStateOf("") }

    val selectedMembers = remember { mutableStateOf(listOf<String>()) }

    var selectedDate: LocalDate? by remember { mutableStateOf(null) }
    var dateText by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))) }
    var showCalendarDialog by remember { mutableStateOf(false) }

    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var durationInMinutes by remember { mutableStateOf(0) }

    val place = remember { mutableStateOf("") }

    val agendaList = remember { mutableStateListOf<String>() }

    val breakTime = remember { mutableStateOf("") }
    val breakTimeMinute = remember { mutableStateOf("") }

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
                        .height(80.dp)
                        .padding(top = 10.dp, bottom = 20.dp),
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
                SearchScreen(
                    selectedEmails = selectedMembers,
                    userApi = userViewModel.userApi
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

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .height(55.dp)
                        .background(Color.White, RoundedCornerShape(15.dp))
                        .border(1.dp, Color.Gray, RoundedCornerShape(15.dp))
                        .clickable(
                            onClick = { showCalendarDialog = true },
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dateText,
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
                TimeDurationPicker(
                    onStartTimeChanged = { startTime = it },
                    onEndTimeChanged = { endTime = it },
                    onDurationChanged = { durationInMinutes = it }
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

                AgendaListScreen(agendaList = agendaList)
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

                    BreakTimeRow(breakTime = breakTime, breakTimeMinute = breakTimeMinute)
                }

            }

            item {
                Button(modifier = Modifier.fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp, top = 15.dp, bottom = 8.dp)
                    .height(60.dp)
                    .border(BorderStroke(0.dp, Color.Transparent)),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.Transparent,
                        contentColor = Color.Black
                    ),
                    elevation = null,
                    onClick = {
                        val agendas = agendaList.toList()
                        val participants = selectedMembers.value

                        if (agendas.isEmpty() || participants.isEmpty()) {
                            Log.e("MeetingCreate", "참석자나 아젠다가 비어있음")
                            return@Button
                        }

                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
                        val scheduledStartTime = LocalDateTime.parse("${dateText}T$startTime", formatter)

                        val meetingReq = MeetingReq(
                            title = meetingTitle.value,
                            leader = leaderName.value,
                            location = place.value,
                            targetTime = durationInMinutes,
                            restInterval = breakTime.value.toIntOrNull() ?: 0,
                            scheduledStartTime = scheduledStartTime.toString(),
                            agendas = agendas,
                            participants = participants
                        )


                        meetingViewModel.createMeeting(
                            meetingReq = meetingReq,
                            onSuccess = {
                                navController.navigate("CompleteNewMeeting")
                            },
                            onError = { errorMessage ->
                                Log.e("MeetingCreate", errorMessage)
                            }
                        )
                    }) {
                    Text("생성하기", style = TextStyle(color = md_theme_button_color_blue, fontSize = 25.sp))
                }
            }

        }

        if (showCalendarDialog) {
            Dialog(onDismissRequest = { showCalendarDialog = false }) {
                val defaultSelected = selectedDate ?: LocalDate.now()
                DataPickerCalendar(
                    navController = navController,
                    initialSelectedDate = defaultSelected,
                    onDateSelected = { date, isConfirmed ->
                        if (isConfirmed) {
                            selectedDate = date
                            dateText = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        }
                        showCalendarDialog = false
                    }
                )
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



