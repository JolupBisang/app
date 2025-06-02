package com.imhungry.jjongseol.ui.meetingdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.runtime.*
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
import com.imhungry.jjongseol.data.model.meeting.MeetingStatus
import com.imhungry.jjongseol.ui.home.DataPickerCalendar
import com.imhungry.jjongseol.ui.newmeeting.CustomBackButton
import com.imhungry.jjongseol.ui.newmeeting.agenda.AgendaListScreen
import com.imhungry.jjongseol.ui.newmeeting.breaktime.BreakTimeRow
import com.imhungry.jjongseol.ui.newmeeting.dateandtime.TimeDurationPicker
import com.imhungry.jjongseol.ui.newmeeting.invite.SearchScreen
import com.imhungry.jjongseol.ui.theme.Purple1
import com.imhungry.jjongseol.ui.theme.Purple2
import com.imhungry.jjongseol.ui.theme.UserGray
import com.imhungry.jjongseol.ui.theme.UserGreen1
import com.imhungry.jjongseol.ui.theme.UserGreen2
import com.imhungry.jjongseol.viewmodel.UserViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun MeetingDetailScreen(
    navController: NavController,
    id: Long,
    title: MutableState<String> = remember { mutableStateOf("제목 없음") },
    location: MutableState<String> = remember { mutableStateOf("장소 없음") },
    participants: List<String> = emptyList(),
    date: MutableState<String> = remember { mutableStateOf("YYYY-MM-DD") },
    startTime: MutableState<String> = remember { mutableStateOf("HH:MM") },
    endTime: MutableState<String> = remember { mutableStateOf("HH:MM") },
    totalTime: MutableState<Int> = remember { mutableStateOf(0) },
    restInterval: MutableState<String> = remember { mutableStateOf("0") },
    restDuration: MutableState<String> = remember { mutableStateOf("0") },
    agendas: List<String> = emptyList(),
    status: MutableState<String> = remember { mutableStateOf("미정") },
    isHost: Boolean,
    isEditable: Boolean,
    onEditClicked: () -> Unit
) {
    var showCalendarDialog by remember { mutableStateOf(false) }

    ConstraintLayout(
        modifier = Modifier
            .background(Color.White)
            .fillMaxSize()
    ) {
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
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .padding(top = 10.dp, bottom = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "회의 정보",
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
                        value = title.value,
                        onValueChange = { if (isEditable) title.value = it },
                        readOnly = !isEditable,
                        modifier = Modifier
                            .background(Color.White, RoundedCornerShape(10.dp))
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
                            unfocusedBorderColor = Color.Transparent,
                            disabledBorderColor = Color.Transparent
                        )
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .padding(top = 8.dp)
                ) {
                    Text(
                        "참석자",
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
                            selectedEmails = remember { mutableStateOf(participants) },
                            userApi = hiltViewModel<UserViewModel>().userApi,
                            enabled = isEditable
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

                    val calendarModifier = if (isEditable) {
                        Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .background(Color.White, RoundedCornerShape(10.dp))
                            .border(1.dp, Color.Gray, RoundedCornerShape(10.dp))
                            .clickable(
                                onClick = { showCalendarDialog = true },
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            )
                    } else {
                        Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .background(Color.White, RoundedCornerShape(10.dp))
                            .border(1.dp, Color.Gray, RoundedCornerShape(10.dp))
                    }

                    Row(
                        modifier = calendarModifier.weight(5f),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = date.value,
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
                            durationInMinutes = totalTime,
                            enabled = isEditable
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
                        value = location.value,
                        onValueChange = { if (isEditable) location.value = it },
                        readOnly = !isEditable,
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
                            unfocusedBorderColor = Color.Transparent,
                            disabledBorderColor = Color.Transparent
                        )
                    )
                }
            }
            item {
                Row(
                    modifier = Modifier
                        .padding(top = 8.dp)
                )
                {
                    Text(
                        "아젠다",
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
                        AgendaListScreen(agendaList = remember { mutableStateListOf(*agendas.toTypedArray()) }, enabled = isEditable)
                    }
                }
            }

            item {
                Column(modifier = Modifier) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(top = 8.dp)
                    )
                    {
                        Text(
                            "쉬는 시간",
                            modifier = Modifier
                                .height(20.dp)
                                .weight(1f),
                            style = TextStyle(
                                color = Color.Black,
                                fontSize = 14.sp,
                            )
                        )

                        Row(modifier = Modifier.weight(5f)) {
                            BreakTimeRow(breakTime = restInterval, breakTimeMinute = restDuration, enabled = isEditable)
                        }
                    }
                }

            }

            item{
                Row(modifier = Modifier
                        .padding(top = 15.dp, bottom = 10.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (isEditable) {
                        Button(
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .border(1.dp, UserGray, RoundedCornerShape(15.dp)),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = UserGray,
                                contentColor = Color.Black
                            ),
                            onClick = { onEditClicked() }
                        ) {
                            Text("취소", style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp))
                        }

                        Button(
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .border(1.dp, UserGreen1, RoundedCornerShape(15.dp)),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = UserGreen1,
                                contentColor = Color.White
                            ),
                            onClick = {
                                onEditClicked()
                            }
                        ) {
                            Text("확인", style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp))
                        }
                    } else {
                        if (isHost) {
                            Button(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .border(1.dp, UserGray, RoundedCornerShape(15.dp)),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = UserGray,
                                    contentColor = Color.Black
                                ),
                                onClick = { navController.popBackStack() }
                            ) {
                                Text("취소", style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp))
                            }
                        }

                        Button(
                            modifier = Modifier
                                .weight(if (isHost) 1f else 1.5f)
                                .height(50.dp)
                                .border(1.dp, UserGreen1, RoundedCornerShape(15.dp)),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = UserGreen1,
                                contentColor = Color.White
                            ),
                            onClick = {
                                when (MeetingStatus.valueOf(status.value)) {
                                    MeetingStatus.WAITING -> navController.navigate("meetingRoute/waiting/$id")
                                    MeetingStatus.IN_PROGRESS -> navController.navigate("meetingRoute/inprogress/$id")
                                    MeetingStatus.COMPLETED -> navController.navigate("meetingRoute/completed/$id")
                                    else -> {}
                                }
                            }
                        ) {
                            Text("입장", style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp))
                        }

                        if (isHost) {
                            Button(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .border(1.dp, UserGreen2, RoundedCornerShape(15.dp)),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = UserGreen2,
                                    contentColor = Color.Black
                                ),
                                onClick = onEditClicked
                            ) {
                                Text("수정", style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp))
                            }
                        }
                    }
                }
            }


        }

        if (showCalendarDialog) {
            Dialog(onDismissRequest = { showCalendarDialog = false }) {
                val defaultSelected = LocalDate.parse(date.value)
                DataPickerCalendar(
                    navController = navController,
                    initialSelectedDate = defaultSelected,
                    onDateSelected = { selected, isConfirmed ->
                        if (isConfirmed) {
                            date.value = selected.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
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
                    top = 40.dp
                )
        ) {
            CustomBackButton(onClick = { navController.popBackStack() })
        }
    }
}
