package com.imhungry.sillok.presentation.screen.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imhungry.sillok.R
import com.imhungry.sillok.ui.components.Divider
import com.imhungry.sillok.ui.components.SillokTextButton
import com.imhungry.sillok.ui.theme.beige
import com.imhungry.sillok.ui.theme.primaryTextColor
import com.imhungry.sillok.ui.theme.sideBar

@Composable
fun Sidebar(
    userName: String = "",
    onNewMeeting: () -> Unit = {},
    onTeamManagement: () -> Unit = {},
    onFeedbackHistory: () -> Unit = {},
    onMeetingFolder: () -> Unit = {},
    onSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(sideBar)
            .width(280.dp)
            .padding(24.dp)
    ) {
        Text(
            text = if (userName.isNotEmpty()) userName else "사용자",
            style = MaterialTheme.typography.titleMedium,
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
