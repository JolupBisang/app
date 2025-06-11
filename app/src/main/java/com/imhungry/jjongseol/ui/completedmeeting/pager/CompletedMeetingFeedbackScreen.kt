package com.imhungry.jjongseol.ui.completedmeeting.pager

import Divider
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.imhungry.jjongseol.data.model.feedback.response.FeedbackListRes
import com.imhungry.jjongseol.ui.completedmeeting.component.MeetingTabRow
import com.imhungry.jjongseol.ui.component.feedback.Notification
import com.imhungry.jjongseol.ui.theme.primaryBackground
import com.imhungry.jjongseol.util.DateTimeUtils
import com.imhungry.jjongseol.viewmodel.FeedbackViewModel

@Composable
fun CompletedMeetingFeedbackScreen(
    feedbackList: List<FeedbackListRes>,
    startMillis: Long,
    selectedTab: Int,
    onTabClick: (Int) -> Unit,
    onTimeClick: (Long) -> Unit,
    feedbackViewModel: FeedbackViewModel,
    meetingId: Long
) {
    val context = LocalContext.current
    val isLoading by feedbackViewModel.isLoading.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(primaryBackground)
            .padding(WindowInsets.statusBars.asPaddingValues())
    ) {
        MeetingTabRow(selectedTab = selectedTab, onTabClick = onTabClick)
        Divider()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(feedbackList) { index, feedback ->
                val elapsedMillis = DateTimeUtils.isoToMillis(feedback.timestamp) - startMillis
                val elapsed = DateTimeUtils.getElapsedString(startMillis, feedback.timestamp)
                if (index == 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
                Notification(
                    visible = true,
                    message = feedback.comment,
                    time = elapsed,
                    isRead = true,
                    onTimeClick = { onTimeClick(elapsedMillis.coerceAtLeast(0L)) }
                )
                if (index == feedbackList.lastIndex) {
                    Spacer(modifier = Modifier.height(48.dp))
                    if (!isLoading) {
                        LaunchedEffect(key1 = feedbackList.size) {
                            feedbackViewModel.loadFeedbacks(meetingId, reset = false)
                        }
                    }
                }
            }
        }
    }
}

