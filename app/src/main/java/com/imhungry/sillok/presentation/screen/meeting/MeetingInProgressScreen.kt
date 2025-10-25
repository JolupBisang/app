package com.imhungry.sillok.presentation.screen.meeting

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.imhungry.sillok.presentation.permission.HandleVoicePermissions
import com.imhungry.sillok.presentation.screen.meeting.component.MeetingControlPanel
import com.imhungry.sillok.presentation.screen.meeting.pager.MeetingFeedbackScreen
import com.imhungry.sillok.presentation.screen.meeting.pager.MeetingRecordScreen
import com.imhungry.sillok.presentation.screen.meeting.pager.MeetingSummaryScreen
import com.imhungry.sillok.presentation.viewmodel.meeting.MeetingInProgressViewModel
import com.imhungry.sillok.ui.components.MeetingBasicBox
import com.imhungry.sillok.ui.theme.green200
import com.imhungry.sillok.ui.theme.orange100
import com.imhungry.sillok.ui.theme.pagerIndicatorBackground
import com.imhungry.sillok.ui.theme.primaryBackground
import com.imhungry.sillok.ui.theme.whiteBackground

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalPagerApi::class)
@Composable
fun MeetingInProgressScreen(
    meetingId: Long,
    onStopMeeting: () -> Unit,
    onCompleteMeeting: () -> Unit,
    meetingInProgressViewModel: MeetingInProgressViewModel = hiltViewModel()
) {
    HandleVoicePermissions()
    LaunchedEffect(Unit) {
        meetingInProgressViewModel.initialize(meetingId)
    }

    val pagerState = rememberPagerState(initialPage = 1)
    var hasUnreadFeedback by remember { mutableStateOf(false) }
    val context = LocalContext.current

    MeetingBasicBox(
        navigationBarColor = whiteBackground,
        backgroundColor = primaryBackground
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(bottom = 20.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                HorizontalPager(
                    count = 3,
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    when (page) {
                        0 -> MeetingSummaryScreen(meetingInProgressViewModel = meetingInProgressViewModel)
                        1 -> MeetingRecordScreen(meetingInProgressViewModel = meetingInProgressViewModel)
                        2 -> MeetingFeedbackScreen(meetingInProgressViewModel = meetingInProgressViewModel)
                    }
                }
                CustomHorizontalPagerIndicator(
                    pagerState = pagerState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 4.dp),
                    unreadFeedback = hasUnreadFeedback,
                )
            }

            MeetingControlPanel(
                timeText = "00:00:00",
                remainingTimeText = "01:10:00",
                onBack = onStopMeeting,
                onComplete = onCompleteMeeting,
                meetingInProgressViewModel = meetingInProgressViewModel
            )
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun CustomHorizontalPagerIndicator(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    pageCount: Int = pagerState.pageCount,
    activeColor: Color = green200,
    inactiveColor: Color = primaryBackground,
    backgroundColor: Color = pagerIndicatorBackground,
    indicatorSize: Int = 8,
    indicatorSpacing: Int = 7,
    paddingHorizontal: Int = 10,
    paddingVertical: Int = 7,
    unreadFeedback: Boolean = false,
    unreadIndicatorColor: Color = orange100,
) {
    Box(
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = CircleShape
            )
            .padding(horizontal = paddingHorizontal.dp, vertical = paddingVertical.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(indicatorSpacing.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(pageCount) { index ->
                val isActive = pagerState.currentPage == index
                val isUnreadFeedback = (index == 2) && unreadFeedback && !isActive
                Box(
                    modifier = Modifier
                        .size(indicatorSize.dp)
                        .background(
                            color = when {
                                isActive -> activeColor
                                isUnreadFeedback -> unreadIndicatorColor
                                else -> inactiveColor
                            },
                            shape = CircleShape
                        )
                )
            }
        }
    }
}
