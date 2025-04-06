package com.imhungry.jjongseol.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeSideBar(drawerState: DrawerState) {
    val scope = rememberCoroutineScope()
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val drawerWidth = screenWidth * 0.6f

    Column(
        modifier = Modifier
            .width(drawerWidth)
            .fillMaxHeight()
            .background(Color.White),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Column(modifier = Modifier.padding(top = 40.dp, start = 20.dp)){
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween){
                Text(
                    "김작가 >",
                    style = TextStyle(fontSize = 30.sp),
                    modifier = Modifier
                        .clickable { }
                )
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "설정",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { }
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                "새 회의 만들기",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { }
                    .padding(vertical = 10.dp, horizontal = 20.dp),
                style = TextStyle(fontSize = 20.sp)
            )
            Text(
                "팀 관리",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { }
                    .padding(vertical = 10.dp, horizontal = 20.dp),
                style = TextStyle(fontSize = 20.sp)
            )
            Text(
                "피드백 기록",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { }
                    .padding(vertical = 10.dp, horizontal = 20.dp),
                style = TextStyle(fontSize = 20.sp)
            )
            Text(
                "회의록 폴더",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { }
                    .padding(vertical = 10.dp, horizontal = 20.dp),
                style = TextStyle(fontSize = 20.sp)
            )
            Spacer(Modifier.weight(1f))
            Text(
                "고객센터",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { }
                    .padding(top=20.dp,bottom = 30.dp, start = 20.dp),
                style = TextStyle(fontSize = 20.sp)
            )
        }
    }
}
