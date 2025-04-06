package com.imhungry.jjongseol.ui.newmeeting

import SearchScreen
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavController
import com.imhungry.jjongseol.ui.newmeeting.agenda.AgendaListScreen
import com.imhungry.jjongseol.ui.newmeeting.breaktime.BreakTimeRow
import com.imhungry.jjongseol.ui.newmeeting.dateandtime.DatePickerField
import com.imhungry.jjongseol.ui.newmeeting.dateandtime.TimeDurationPicker
import com.imhungry.jjongseol.ui.theme.md_theme_button_color_blue

@Composable
fun CreateNewMeetingScreen(navController: NavController){
    val meetingTitle = remember { mutableStateOf("") }
    val leaderName = remember { mutableStateOf("") }
    //val memberLists = remember { mutableStateOf("") }
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
                SearchScreen()
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
                        navController.navigate("CompleteNewMeeting")
                    }) {
                    Text("생성하기", style = TextStyle(color = md_theme_button_color_blue, fontSize = 25.sp))
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



