package com.imhungry.sillok.presentation.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.imhungry.sillok.ui.components.Divider
import com.imhungry.sillok.ui.components.SillokTextButton
import com.imhungry.sillok.ui.components.SystemBars
import com.imhungry.sillok.ui.theme.placeHolder
import com.imhungry.sillok.ui.theme.sideBar

@Composable
fun Sidebar(
    userName: String = "",
    profileImage: String = "",
    onNewMeeting: () -> Unit = {},
    onTeamManagement: () -> Unit = {},
    onFeedbackHistory: () -> Unit = {},
    onMeetingFolder: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .background(sideBar)
            .windowInsetsPadding(WindowInsets.systemBars)
            .width(280.dp)
            .padding(24.dp)
    ) {
        // 프로필 이미지
        AsyncImage(
            model = profileImage.ifEmpty { null },
            contentDescription = "프로필 이미지",
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(placeHolder),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = userName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))
        Divider()
        Spacer(modifier = Modifier.height(28.dp))

        MenuItem(
            text = "새 회의 만들기",
            onClick = onNewMeeting
        )

        Spacer(modifier = Modifier.height(24.dp))

        MenuItem(
            text = "팀 관리",
            onClick = onTeamManagement
        )

        Spacer(modifier = Modifier.height(24.dp))

        MenuItem(
            text = "피드백 기록",
            onClick = onFeedbackHistory
        )

        Spacer(modifier = Modifier.height(24.dp))

        MenuItem(
            text = "회의록 폴더",
            onClick = onMeetingFolder
        )
    }
}

@Composable
private fun MenuItem(
    text: String,
    onClick: () -> Unit
) {
    SillokTextButton(
        text = text,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        fontSize = 16
    )
}
