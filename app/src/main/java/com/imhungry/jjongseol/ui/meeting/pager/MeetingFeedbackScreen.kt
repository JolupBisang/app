package com.imhungry.jjongseol.ui.meeting.pager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imhungry.jjongseol.ui.component.feedback.Notification
import com.imhungry.jjongseol.ui.theme.Pretend
import com.imhungry.jjongseol.viewmodel.MeetingViewModel

@Composable
fun MeetingFeedbackScreen(meetingViewModel: MeetingViewModel) {
    val feedbackList by meetingViewModel.feedbackList.collectAsState()
    DisposableEffect(Unit) {
        onDispose {
            //markAllFeedbackAsRead()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE5E5E5))
            .padding(WindowInsets.statusBars.asPaddingValues())
            .background(Color.White)
    ) {
        Text(
            text = "피드백",
            fontFamily = Pretend,
            fontWeight = FontWeight.ExtraBold,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 15.sp,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top =  16.dp, bottom = 12.dp)
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(feedbackList) { index, feedback ->
                Notification(
                    visible = true,
                    message = feedback.comment,
                    time = feedback.timestamp,
                    isRead = feedback.isRead
                )
                if (index == feedbackList.lastIndex) {
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    }
}

