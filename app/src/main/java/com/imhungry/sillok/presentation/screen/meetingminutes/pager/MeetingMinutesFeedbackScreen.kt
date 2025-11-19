package com.imhungry.sillok.presentation.screen.meetingminutes.pager

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.imhungry.sillok.domain.model.feedback.Feedback
import com.imhungry.sillok.presentation.screen.meeting.component.Notification
import com.imhungry.sillok.presentation.screen.meetingminutes.components.MeetingTabRow
import com.imhungry.sillok.presentation.state.meeting.FeedbackUi
import com.imhungry.sillok.presentation.util.DateTimeUtils
import com.imhungry.sillok.presentation.viewmodel.meetingminutes.MeetingMinutesViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MeetingMinutesFeedbackScreen(
    selectedTab: Int,
    onTabClick: (Int) -> Unit,
    onTimeClick: (Long) -> Unit, // millis
    meetingMinutesViewModel: MeetingMinutesViewModel
) {
    val state by meetingMinutesViewModel.state.collectAsState()
    val feedbacksPagingFlowState = meetingMinutesViewModel.feedbacksPagingFlow
    val feedbacksPagingFlow by feedbacksPagingFlowState.collectAsState()
    
    // Paging Items
    val pagingItems: LazyPagingItems<Feedback>? = feedbacksPagingFlow?.let { 
        it.collectAsLazyPagingItems() 
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        MeetingTabRow(selectedTab = selectedTab, onTabClick = onTabClick)

        if (pagingItems != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    count = pagingItems.itemCount,
                    key = pagingItems.itemKey { it.id },
                    contentType = pagingItems.itemContentType { "feedback" }
                ) { index ->
                    val feedback = pagingItems[index] ?: return@items
                    val feedbackUi = meetingMinutesViewModel.convertFeedbackToUi(
                        feedback = feedback,
                        startMillis = state.startMillis
                    )
                    
                    if (index == 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    Notification(
                        feedback = feedbackUi,
                        isRead = true,
                        onTimeClick = {
                            // 피드백의 timestamp를 밀리초로 변환하여 오디오 재생 위치로 이동
                            val seekMillis = DateTimeUtils.timeStringToMillis(feedbackUi.timestamp)
                            if (seekMillis != null) {
                                onTimeClick(seekMillis.coerceAtLeast(0L))
                            }
                        }
                    )
                    if (index == pagingItems.itemCount - 1) {
                        Spacer(modifier = Modifier.height(48.dp))
                    }
                }
            }
        }
    }
}

