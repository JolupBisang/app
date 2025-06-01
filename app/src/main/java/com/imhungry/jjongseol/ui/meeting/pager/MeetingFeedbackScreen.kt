package com.imhungry.jjongseol.ui.meeting.pager

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.imhungry.jjongseol.ui.component.feedback.Notification
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
            .fillMaxSize()
            .padding(top = 56.dp, bottom = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        feedbackList.forEach { feedback ->
            Notification(
                visible = true,
                message = feedback.comment,
                time = feedback.timestamp,
                isRead = feedback.isRead
            )
        }
    }
}

