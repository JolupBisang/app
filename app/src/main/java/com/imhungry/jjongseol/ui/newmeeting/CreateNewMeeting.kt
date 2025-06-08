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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import com.imhungry.jjongseol.data.model.meeting.MeetingReq
import com.imhungry.jjongseol.data.network.api.MeetingUserApi
import com.imhungry.jjongseol.data.network.config.RetrofitModule
import com.imhungry.jjongseol.ui.home.DataPickerCalendar
import com.imhungry.jjongseol.ui.newmeeting.agenda.AgendaListScreen
import com.imhungry.jjongseol.ui.newmeeting.breaktime.BreakTimeRow
import com.imhungry.jjongseol.ui.newmeeting.dateandtime.TimeDurationPicker
import com.imhungry.jjongseol.ui.newmeeting.invite.SearchScreen
import com.imhungry.jjongseol.ui.theme.Purple1
import com.imhungry.jjongseol.ui.theme.UserGreen1
import com.imhungry.jjongseol.viewmodel.MeetingViewModel
import com.imhungry.jjongseol.viewmodel.UserViewModel
import dagger.hilt.android.EntryPointAccessors
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun CreateNewMeetingScreen(navController: NavController){
    val meetingViewModel: MeetingViewModel = hiltViewModel()
    val userViewModel: UserViewModel = hiltViewModel()

    val context = LocalContext.current
    val meetingUserApi = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            RetrofitModule.MeetingUserApiEntryPoint::class.java
        ).meetingUserApi()
    }

    val meetingTitle = remember { mutableStateOf("") }

    val selectedMembers = remember { mutableStateOf(listOf<String>()) }

    var selectedDate: LocalDate? by remember { mutableStateOf(null) }
    var dateText by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))) }
    var showCalendarDialog by remember { mutableStateOf(false) }

    val startTime = remember { mutableStateOf("HH:MM") }
    val endTime = remember { mutableStateOf("HH:MM") }
    val duration = remember { mutableStateOf(0) }

    val place = remember { mutableStateOf("") }

    val agendaList = remember { mutableStateListOf<String>() }

    val breakTime = remember { mutableStateOf("") }
    val breakTimeMinute = remember { mutableStateOf("") }

    val isTitleError = remember { mutableStateOf(false) }
    val isTimeError = remember { mutableStateOf(false) }
    val isPlaceError = remember { mutableStateOf(false) }
    val isAgendaError = remember { mutableStateOf(false) }
    val isAgendaLengthError = remember { mutableStateOf(false) }
    val isBreakTimeError = remember { mutableStateOf(false) }

    val isHost by meetingViewModel.isHost.collectAsState()

    val userInfo by userViewModel.userInfo.collectAsState()
    val myEmail = userInfo?.email.orEmpty()

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
                            color = Color.Black,
                            fontSize = 30.sp,
                        )
                    )
                }
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .height(20.dp)
                            .weight(1f),
                    )
                    {
                        Text(
                            "제목",
                            style = TextStyle(
                                color = Color.Black,
                                fontSize = 15.sp,
                            )
                        )
                    }

                    OutlinedTextField(
                        value = meetingTitle.value,
                        onValueChange = { meetingTitle.value = it },
                        modifier = Modifier
                            .border(1.dp, Color.Gray, RoundedCornerShape(10.dp))
                            .height(50.dp)
                            .weight(5f),
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
                ValidationErrorText(isTitleError.value)
            }

            item {
                Row(
                    modifier = Modifier
                        .padding(top = 8.dp)
                ) {
                    Text(
                        text = "참석자",
                        modifier = Modifier
                            .padding(top = 15.dp)
                            .height(20.dp)
                            .weight(1f),
                        style = TextStyle(
                            color = Color.Black,
                            fontSize = 15.sp,
                        )
                    )

                    Column(modifier = Modifier.weight(5f)) {
                        SearchScreen(
                            meetingId = -1L,
                            selectedEmails = selectedMembers,
                            userApi = userViewModel.userApi,
                            meetingUserApi = meetingUserApi,
                            enabled = true,
                            hostEmail = myEmail
                        )
                    }
                }
            }
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(top = 8.dp)
                )
                {
                    Text(
                        "일시",
                        modifier = Modifier
                            .height(20.dp)
                            .weight(1f),
                        style = TextStyle(
                            color = Color.Black,
                            fontSize = 15.sp,
                        )
                    )


                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .background(Color.White, RoundedCornerShape(10.dp))
                            .border(1.dp, Color.Gray, RoundedCornerShape(10.dp))
                            .clickable(
                                onClick = { showCalendarDialog = true },
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            )
                            .weight(5f),
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
            }



            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(top = 8.dp)
                )
                {
                    Text(
                        "시간",
                        modifier = Modifier
                            .height(20.dp)
                            .weight(1f),
                        style = TextStyle(
                            color = Color.Black,
                            fontSize = 15.sp,
                        )
                    )

                    Row(modifier = Modifier.weight(5f)) {
                        TimeDurationPicker(
                            startTime = startTime,
                            endTime = endTime,
                            durationInMinutes = duration,
                            enabled = true
                        )
                    }
                }
                ValidationErrorText(isTimeError.value)
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(top = 8.dp)
                )
                {
                    Text(
                        "장소",
                        modifier = Modifier
                            .height(20.dp)
                            .weight(1f),
                        style = TextStyle(
                            color = Color.Black,
                            fontSize = 15.sp,
                        )
                    )

                    OutlinedTextField(
                        value = place.value,
                        onValueChange = { place.value = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.Gray, RoundedCornerShape(10.dp))
                            .weight(5f),
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 16.sp),
                        placeholder = {
                            Text(
                                "장소",
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
                ValidationErrorText(isPlaceError.value)
            }

            item {
                Row(
                    modifier = Modifier
                        .padding(top = 8.dp)
                )
                {
                    Text("아젠다",
                        modifier = Modifier
                            .padding(top = 15.dp)
                            .height(20.dp)
                            .weight(1f),
                        style = TextStyle(
                            color = Color.Black,
                            fontSize = 15.sp,
                        )
                    )
                    Row(modifier = Modifier.weight(5f)) {
                        AgendaListScreen(agendaList = agendaList, enabled = true)
                    }
                }
                ValidationErrorText(isAgendaError.value)
                ValidationErrorText(isAgendaLengthError.value, message = "회의 안건은 1글자 이상이어야 합니다.")
            }

            item{
                Column(modifier = Modifier) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(top = 8.dp)
                    )
                    {
                        Text(
                            "쉬는시간",
                            modifier = Modifier
                                .height(20.dp)
                                .weight(1f),
                            style = TextStyle(
                                color = Color.Black,
                                fontSize = 14.sp,
                            )
                        )

                        Row(modifier = Modifier.weight(5f)) {
                            BreakTimeRow(breakTime = breakTime, breakTimeMinute = breakTimeMinute, enabled = true)
                        }
                    }
                    ValidationErrorText(isBreakTimeError.value)
                }

            }

            item {
                Button(modifier = Modifier.fillMaxWidth()
                    .padding(top = 15.dp, bottom = 8.dp)
                    .height(55.dp)
                    .border(1.dp, UserGreen1, RoundedCornerShape(13.dp)),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = UserGreen1,
                        contentColor = Color.White
                    ),
                    elevation = null,
                    onClick = {
                        val agendas = agendaList.toList()
                        val participants = selectedMembers.value

                        var hasError = false
                        isTitleError.value = meetingTitle.value.isBlank().also { if (it) hasError = true }
                        isTimeError.value = startTime.value == "HH:MM" || endTime.value == "HH:MM"
                        if (isTimeError.value) hasError = true
                        isPlaceError.value = place.value.isBlank().also { if (it) hasError = true }
                        isAgendaError.value = agendas.isEmpty().also { if (it) hasError = true }
                        isAgendaLengthError.value = agendas.any { it.trim().length < 1 }
                        if (isAgendaLengthError.value) hasError = true
                        isBreakTimeError.value = breakTime.value.isBlank() || breakTimeMinute.value.isBlank()
                        if (isBreakTimeError.value) hasError = true

                        if (hasError) return@Button

                        if (agendas.isEmpty()) {
                            Log.e("MeetingCreate", "아젠다가 비어있음")
                            return@Button
                        }

                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
                        val scheduledStartTime =LocalDateTime.parse("${dateText}T${startTime.value}", formatter)

                        val meetingReq = MeetingReq(
                            title = meetingTitle.value,
                            location = place.value,
                            targetTime = duration.value,
                            restInterval = breakTime.value.toIntOrNull() ?: 0,
                            restDuration = breakTimeMinute.value.toIntOrNull() ?: 0,
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
                    Text("새 회의 등록", style = TextStyle(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp))
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

@Composable
fun ValidationErrorText(visible: Boolean, message: String = "필수항목이 작성되지 않았습니다.", modifier: Modifier = Modifier) {
    if (visible) {
        Text(
            message,
            color = Color.Red,
            fontSize = 12.sp,
            modifier = modifier.padding(start = 70.dp, top = 4.dp)
        )
    }
}

