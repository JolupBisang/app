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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.imhungry.sillok.presentation.screen.meeting.component.Notification
import com.imhungry.sillok.presentation.screen.meetingminutes.components.MeetingTabRow
import com.imhungry.sillok.presentation.util.DateTimeUtils
import com.imhungry.sillok.presentation.viewmodel.meetingminutes.MeetingMinutesViewModel
import com.imhungry.sillok.ui.components.Divider

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MeetingMinutesFeedbackScreen(
    selectedTab: Int,
    onTabClick: (Int) -> Unit,
    onTimeClick: (Long) -> Unit,
    meetingMinutesViewModel: MeetingMinutesViewModel
) {
    val state by meetingMinutesViewModel.state.collectAsState()
    val feedbacks = state.feedbacks

    Column(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        MeetingTabRow(selectedTab = selectedTab, onTabClick = onTabClick)
        Divider()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(feedbacks) { index, feedback ->
                if (index == 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
                Notification(
                    feedback = feedback,
                    isRead = true
                )
                if (index == feedbacks.lastIndex) {
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    }
}

