package com.imhungry.jjongseol.ui.home

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.imhungry.jjongseol.ui.theme.BasicBackGround
import com.imhungry.jjongseol.ui.theme.SkyBlue
import com.imhungry.jjongseol.ui.theme.UserGreen1
import com.imhungry.jjongseol.viewmodel.UserViewModel

@Composable
fun HomeSideBar(drawerState: DrawerState, navController: NavController) {
    val scope = rememberCoroutineScope()
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val drawerWidth = screenWidth * 0.6f

    val userViewModel: UserViewModel = hiltViewModel()
    val nickname by userViewModel.nickname.collectAsState()

    LaunchedEffect(Unit) {
        userViewModel.loadMyNickname2()
    }

    Column(
        modifier = Modifier
            .width(drawerWidth)
            .fillMaxHeight()
            .background(BasicBackGround),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Column(modifier = Modifier.padding(top = 30.dp, start = 20.dp, end = 10.dp)){
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(top = 30.dp, bottom = 5.dp, start = 5.dp, end = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween){
                Text(
                    text = nickname ?: "로딩 중...",
                    style = TextStyle(fontSize = 20.sp),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { }
                )
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "설정",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { },
                    tint = UserGreen1
                )
            }
            TabRowDefaults.Divider(
                color = Color.Gray,
                thickness = 0.5.dp,
                modifier = Modifier.padding(top = 10.dp, bottom = 5.dp, end = 10.dp)
            )
            Spacer(Modifier.height(24.dp))
            Text(
                "새 회의 만들기",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate("createNewMeeting") }
                    .padding(vertical = 10.dp, horizontal = 10.dp),
                style = TextStyle(fontSize = 18.sp)
            )
            Text(
                "팀 관리",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { }
                    .padding(vertical = 10.dp, horizontal = 10.dp),
                style = TextStyle(fontSize = 18.sp)
            )
            Text(
                "피드백 기록",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { }
                    .padding(vertical = 10.dp, horizontal = 10.dp),
                style = TextStyle(fontSize = 18.sp)
            )
            Text(
                "회의록 폴더",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { }
                    .padding(vertical = 10.dp, horizontal = 10.dp),
                style = TextStyle(fontSize = 18.sp)
            )
            Spacer(Modifier.weight(1f))
            Text(
                "고객센터",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { }
                    .padding(top=20.dp,bottom = 30.dp, start = 10.dp),
                style = TextStyle(fontSize = 20.sp)
            )
        }
    }
}
