package com.imhungry.jjongseol.ui.meeting.pager

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.imhungry.jjongseol.ui.component.feedback.Notification
import com.imhungry.jjongseol.viewmodel.MeetingViewModel

@Composable
fun MeetingFeedbackScreen(viewModel: MeetingViewModel = hiltViewModel()) {
    val feedbackList by viewModel.feedbackList.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.markAllFeedbackAsRead()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 56.dp, bottom = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        feedbackList.forEach { item ->
            Notification(
                visible = true,
                message = item.text,
                time = item.time,
                isRead = item.isRead
            )
        }
    }
}

