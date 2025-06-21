package com.imhungry.jjongseol.ui.meeting.pager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imhungry.jjongseol.data.model.feedback.dto.FeedbackDto
import com.imhungry.jjongseol.data.model.feedback.response.FeedbackListRes
import com.imhungry.jjongseol.ui.component.feedback.Notification
import com.imhungry.jjongseol.ui.theme.Pretend
import com.imhungry.jjongseol.ui.theme.primaryBackground
import com.imhungry.jjongseol.util.DateTimeUtils
import com.imhungry.jjongseol.viewmodel.MeetingViewModel

@Composable
fun MeetingFeedbackScreen(
    meetingViewModel: MeetingViewModel,
    feedbacks: List<FeedbackListRes>,
    startTime: Long?,
    meetingId: Long
) {
    val context = LocalContext.current
    val feedbackList by meetingViewModel.feedbackList.collectAsState()

//    LaunchedEffect(Unit) {
//        if (feedbacks.isNotEmpty() && meetingViewModel.feedbackList.value.isEmpty()) {
//            val dtoList = feedbacks.map {
//                FeedbackDto(
//                    timestamp = it.timestamp,
//                    comment = it.comment,
//                    isRead = true
//                )
//            }
//            meetingViewModel.setFeedbackList(dtoList)
//        }
//    }

//    LaunchedEffect(feedbacks) {
//        if (feedbacks.isNotEmpty()) {
//            val dtoList = feedbacks.map {
//                FeedbackDto(
//                    timestamp = it.timestamp,
//                    comment = it.comment,
//                    isRead = true
//                )
//            }
//            meetingViewModel.setFeedbackList(dtoList)
//        }
//    }
    DisposableEffect(Unit) {
        onDispose {
            meetingViewModel.markAllFeedbackAsRead()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(primaryBackground)
            .padding(WindowInsets.statusBars.asPaddingValues())
    ) {
        Box(
            modifier = Modifier
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 12.dp)
                .drawBehind {
                    val underlineHeight = 7.dp.toPx()
                    drawRect(
                        color = Color(0x40186848),
                        topLeft = Offset(0f, size.height - underlineHeight),
                        size = androidx.compose.ui.geometry.Size(size.width, underlineHeight)
                    )
                }
        ) {
            Text(
                text = "피드백",
                fontFamily = Pretend,
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 15.sp
            )
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(feedbackList) { index, feedback ->
                val elapsed = DateTimeUtils.getElapsedString(startTime, feedback.timestamp)

                Notification(
                    visible = true,
                    message = feedback.comment,
                    time = elapsed,
                    isRead = feedback.isRead
                )
                if (index == feedbackList.lastIndex) {
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    }
}