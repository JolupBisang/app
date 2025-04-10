package com.imhungry.jjongseol.ui.completedmeeting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
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
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import com.imhungry.jjongseol.ui.SilRokNavigation
import com.imhungry.jjongseol.ui.completedmeeting.bottom.CompletedMeetingControlPanel
import com.imhungry.jjongseol.ui.component.CustomSeekBar
import com.imhungry.jjongseol.ui.meeting.pager.MeetingFeedbackScreen
import com.imhungry.jjongseol.ui.meeting.pager.MeetingRecordScreen
import com.imhungry.jjongseol.ui.meeting.pager.MeetingSummaryScreen
import com.imhungry.jjongseol.viewmodel.CompletedMeetingViewModel
import kotlinx.coroutines.delay

@Composable
fun CompletedMeetingScreen(
    viewModel: CompletedMeetingViewModel = hiltViewModel(),
    onFinish: (SilRokNavigation) -> Unit,
) {
    val context = LocalContext.current

    CompletedMeetingContent(
        onFinish = onFinish,
    )
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun CompletedMeetingContent(
    onFinish: (SilRokNavigation) -> Unit,
    viewModel: CompletedMeetingViewModel = hiltViewModel()
) {
    var currentPosition by remember { mutableStateOf(0f) }
    var isPlaying by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableStateOf(1.0f) }
    val duration = 180f

    LaunchedEffect(isPlaying, playbackSpeed) {
        while (isPlaying && currentPosition < duration) {
            delay(100L)
            currentPosition = (currentPosition + 0.1f * playbackSpeed).coerceAtMost(duration)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val pagerState = rememberPagerState(initialPage = 1)

        HorizontalPager(
            count = 3,
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.85f)
        ) { page ->
            when (page) {
                0 -> MeetingSummaryScreen()
                1 -> MeetingRecordScreen()
                2 -> MeetingFeedbackScreen()
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            HorizontalPagerIndicator(
                pagerState = pagerState,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 4.dp, bottom = 4.dp),
                activeColor = Color(0xFF1E93EF),
                inactiveColor = Color.LightGray,
                indicatorWidth = 6.dp,
                spacing = 4.dp
            )
            Spacer(modifier = Modifier.padding(bottom = 4.dp))
            CustomSeekBar(
                currentPosition = currentPosition,
                duration = duration,
                onValueChange = { currentPosition = it }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(0.15f)
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        ) {
            CompletedMeetingControlPanel(
                currentPosition = currentPosition,
                duration = duration,
                onSeek = { currentPosition = it },
                isPlaying = isPlaying,
                onTogglePlay = { isPlaying = !isPlaying },
                modifier = Modifier.fillMaxWidth(),
                playbackSpeed = playbackSpeed,
                onSpeedChange = { playbackSpeed = it }
            )
        }
    }
}


