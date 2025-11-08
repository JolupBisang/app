package com.imhungry.sillok.presentation.screen.meeting.pager

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.imhungry.sillok.presentation.screen.meeting.component.Notification
import com.imhungry.sillok.presentation.viewmodel.meeting.MeetingInProgressViewModel
import com.imhungry.sillok.ui.components.HighlightText

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MeetingFeedbackScreen(
    meetingInProgressViewModel: MeetingInProgressViewModel
) {
    val state by meetingInProgressViewModel.state.collectAsState()
    val feedbacks = state.feedbacks

    // 화면을 벗어날 때 모든 피드백을 읽음 처리
    DisposableEffect(Unit) {
        onDispose {
            // 화면이 제거될 때 읽음 처리
            meetingInProgressViewModel.markAllFeedbacksAsRead()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 20.dp)
    ) {
        HighlightText(
            text = "피드백",
            modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(feedbacks) { index, feedback ->
                Notification(feedback = feedback, isRead = feedback.isRead)
                if (index == feedbacks.lastIndex) {
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    }
}