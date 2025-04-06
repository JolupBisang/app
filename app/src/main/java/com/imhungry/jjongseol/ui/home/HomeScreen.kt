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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavController
import com.imhungry.jjongseol.R
import com.imhungry.jjongseol.ui.theme.md_theme_button_color_blue
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
            HomeSideBar(drawerState)
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

                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp, bottom = 20.dp, end = 10.dp, start = 10.dp)
                        .background(Color.White)
                        .height(70.dp)){
                        Row(verticalAlignment = Alignment.CenterVertically,) {
                            Image(
                                painter = painterResource(id = R.drawable.home_sidebar_menu),
                                contentDescription = "사이드 바 메뉴",
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) {
                                        scope.launch { drawerState.open() }
                                    },
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(5f),
                                contentAlignment = Alignment.Center
                            ) {
                                OutlinedTextField(
                                    value = searchText.value,
                                    onValueChange = { searchText.value = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(30.dp))
                                        .background(Color.LightGray),
                                    singleLine = true,
                                    textStyle = TextStyle(fontSize = 16.sp),
                                    placeholder = {
                                        Text(
                                            "제목, 참석자",
                                            style = TextStyle(color = Color.Gray)
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
                                            modifier = Modifier.clickable { }
                                        )
                                    }
                                )
                            }
                            Image(
                                painter = painterResource(id = R.drawable.announcement_notification),
                                contentDescription = "공지사항 알림",
                                modifier = Modifier
                                    .weight(1f),
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
    ConstraintLayout (modifier = Modifier
        .background(Color.White)
        .fillMaxSize()){
        val (scrollList, bottomArea) = createRefs()

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
                Spacer(Modifier.height(60.dp))
                Divider(
                    color = Color.Gray,
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
                ScheduledMeetingScreen()
                Canvas(modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)) {
                    drawLine(
                        color = Color.Gray,
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                }
                MeetingRecordsScreen()
            }
        }

        //임시 위치..
        Box(
            contentAlignment = Alignment.BottomCenter,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 90.dp)
                .constrainAs(bottomArea) {
                    bottom.linkTo(parent.bottom)
                }
        ) {
            MeetingCard()
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
        shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.LightGray
            ),
            border = BorderStroke(1.dp, Color.LightGray),
            elevation = ButtonDefaults.elevation(
                defaultElevation = 4.dp,
                pressedElevation = 8.dp,
                disabledElevation = 0.dp
            ),
            content = {
                Text(
                    "새 회의",
                    style = TextStyle(
                        color = md_theme_button_color_blue,
                        fontSize = 20.sp
                    )
                )
            }
        )
    }
}

@Composable
fun MeetingCard() {
    var isVisible by remember { mutableStateOf(true) }

    if (isVisible) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .height(180.dp),
            elevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "현재 진행 중인 회의가 있어요!",
                    color = Color.DarkGray,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "종설 회의",
                    color = Color.DarkGray,
                )
                Text(
                    text = "2025.04.01. 18:00~20:00",
                    color = Color.DarkGray,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("참여하지 않기", Modifier.clickable{isVisible = false}
                        .padding(10.dp),
                        color = md_theme_button_color_blue)
                    Spacer(Modifier.weight(1f))
                    Text("바로 참여하기 >", Modifier.clickable {}
                        .padding(10.dp),
                        color = md_theme_button_color_blue)
                }
            }
        }
    }
}