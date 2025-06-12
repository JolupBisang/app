package com.imhungry.jjongseol.ui.home

import android.util.Log
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
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.imhungry.jjongseol.R
import com.imhungry.jjongseol.data.network.config.AppPrefs
import com.imhungry.jjongseol.ui.theme.BasicBackGround
import com.imhungry.jjongseol.ui.theme.TransparentGreen
import com.imhungry.jjongseol.ui.theme.UserGray
import com.imhungry.jjongseol.ui.theme.UserGreen1
import com.imhungry.jjongseol.viewmodel.MeetingViewModel
import kotlinx.coroutines.launch
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.imhungry.jjongseol.ui.theme.SetNavigationBarColor
import com.imhungry.jjongseol.ui.theme.whiteColor
import kotlinx.coroutines.delay


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val searchText = remember { mutableStateOf("") }
    val viewModel: MeetingViewModel = hiltViewModel()
    val context = LocalContext.current
    val appPrefs = remember { AppPrefs(context) }

    var isRunning by remember { mutableStateOf(false) }
    var meetingId by remember { mutableStateOf(-1L) }

    // 앱 진입시 포그라운드 서비스 및 meetingId 상태 확인
    LaunchedEffect(Unit) {
        isRunning = appPrefs.isMeetingForegroundServiceRunning()
        meetingId = appPrefs.getRunningMeetingId()
        Log.d("HomeScreen", "포그라운드 서비스 실행 중? $isRunning, 실행 중인 회의 ID: $meetingId")
    }
    SetNavigationBarColor(Color(0x00FFFFFF))

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
                    MainHomeScreen(
                        navController = navController,
                        isRunning = isRunning,
                        meetingId = meetingId
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 15.dp)
                            .height(50.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
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
                                modifier = Modifier.size(24.dp),
                                tint = UserGreen1
                            )
                        }
                    }

                }
            }
        )
    }
}


@Composable
fun MainHomeScreen(
    navController: NavController,
    isRunning: Boolean,
    meetingId: Long
) {
    val viewModel: MeetingViewModel = hiltViewModel()
    val meetings by viewModel.meetings.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val scope = rememberCoroutineScope()
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isLoading)

    var calendarToggle by remember { mutableStateOf(true) }

    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val (scrollList, bottomArea) = createRefs()

        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = {
                scope.launch {
                    viewModel.refreshMeetings()
                    delay(600)
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .constrainAs(scrollList) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end)
                    start.linkTo(parent.start)
                }
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(top = 30.dp, start = 20.dp, bottom = 50.dp, end = 20.dp)
                    .fillMaxSize()
            ) {
                item {
                    Spacer(Modifier.height(40.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        Text(
                            text = if (calendarToggle) "캘린더" else "리스트",
                            color = UserGreen1,
                            modifier = Modifier
                                .padding(end = 2.dp)
                                .clickable { calendarToggle = !calendarToggle }
                        )
                    }
                    Divider(
                        color = Color.Gray,
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 5.dp)
                    )

                    if (calendarToggle) {
                        ScheduledMeetingScreen(navController)
                        Divider(
                            color = Color.Gray,
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 5.dp)
                        )
                        MeetingRecordsScreen(navController)
                    } else {
                        CalendarScreen(navController)
                    }
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
                    navController.navigate("meetingRoute/inprogress/${meeting.id}") {
                        popUpTo(0)
                    }
                },
                isRunning = isRunning,
                meetingId = meetingId
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

