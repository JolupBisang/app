package com.imhungry.jjongseol.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.imhungry.jjongseol.R
import com.imhungry.jjongseol.ui.theme.BasicBackGround
import com.imhungry.jjongseol.ui.theme.TransparentGreen
import com.imhungry.jjongseol.ui.theme.UserGray
import com.imhungry.jjongseol.ui.theme.UserGreen1
import com.imhungry.jjongseol.viewmodel.MeetingViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val searchText = remember { mutableStateOf("") }


    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            HomeSideBar(drawerState,navController)
        },
    ) {
        Scaffold(
            content = { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    //MakeProfile(navController)
                    //CreateNewMeetingScreen(navController)
                    MainHomeScreen(navController)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 15.dp)
                            .height(50.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "사이드 바 메뉴",
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) {
                                        scope.launch { drawerState.open() }
                                    }
                            )

                            Spacer(modifier = Modifier.width(10.dp))
                            OutlinedTextField(
                                value = searchText.value,
                                onValueChange = { searchText.value = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(BasicBackGround),
                                singleLine = true,
                                textStyle = TextStyle(fontSize = 16.sp),
                                placeholder = {
                                    Text(
                                        "제목, 참석자로 검색",
                                        style = TextStyle(color = Color.DarkGray)
                                    )
                                },
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    textColor = Color.Black,
                                    cursorColor = Color.Black,
                                    backgroundColor = Color.Transparent,
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent
                                ),
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Search,
                                        contentDescription = "검색하기",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Icon(
                                imageVector = Icons.Filled.Notifications,
                                contentDescription = "공지사항 알림",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                }
            }
        )
    }
}


@Composable
fun MainHomeScreen(navController: NavController){
    val viewModel: MeetingViewModel = hiltViewModel()
    val meetings by viewModel.meetings.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadMeetings()
    }

    var calendarToggle by remember { mutableStateOf(true) }

    ConstraintLayout (modifier = Modifier
        .fillMaxSize()){
        val (scrollList, bottomArea) = createRefs()

        LazyColumn(
            modifier = Modifier
                .padding(top = 30.dp, start = 30.dp, bottom = 50.dp, end = 30.dp)
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
                Spacer(Modifier.height(40.dp))
                Row(
                    modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp),
                    horizontalArrangement = Arrangement.End,
                    ){
                    Text(text = if(calendarToggle) "캘린더" else "리스트"
                        ,color = UserGreen1
                        ,modifier = Modifier
                            .padding(end = 5.dp)
                            .clickable { calendarToggle = !calendarToggle })
                }
                Divider(
                    color = Color.Gray,
                    thickness = 1.5.dp,
                    modifier = Modifier.padding(vertical = 5.dp)
                )
                if(calendarToggle) {
                    ScheduledMeetingScreen(navController)

                    Divider(
                        color = Color.Gray,
                        thickness = 1.5.dp,
                        modifier = Modifier.padding(vertical = 5.dp)
                    )
                    MeetingRecordsScreen(navController)

                }
                else{
                    CalendarScreen(navController)
                }
            }
        }

        Box(
            contentAlignment = Alignment.BottomCenter,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 90.dp)
                .padding(horizontal = 10.dp)
                .constrainAs(bottomArea) {
                    bottom.linkTo(parent.bottom)
                }
        ) {
            MeetingCardList(
                meetings = meetings,
                onJoinMeeting = { meeting ->
                    navController.navigate("meetingRoute/inprogress/${meeting.id}")
                }
            )
        }

        Box(
            contentAlignment = Alignment.BottomCenter,
            modifier = Modifier
        ) {
            CreateNewMeetingButton(onClick = { navController.navigate("createNewMeeting") })
        }

    }
}

@Composable
fun CreateNewMeetingButton(onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 30.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 30.dp)
                .height(60.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = UserGreen1
            ),
            border = BorderStroke(1.dp, UserGreen1),
            elevation = ButtonDefaults.elevation(
                defaultElevation = 4.dp,
                pressedElevation = 8.dp,
                disabledElevation = 0.dp
            ),
            content = {
                Text(
                    "새 회의",
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        )
    }
}

