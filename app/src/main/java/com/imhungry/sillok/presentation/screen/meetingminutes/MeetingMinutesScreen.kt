package com.imhungry.sillok.presentation.screen.meetingminutes

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.imhungry.sillok.presentation.screen.meeting.CustomHorizontalPagerIndicator
import com.imhungry.sillok.presentation.screen.meetingminutes.components.AudioPlayerBar
import com.imhungry.sillok.presentation.screen.meetingminutes.pager.MeetingMinutesFeedbackScreen
import com.imhungry.sillok.presentation.screen.meetingminutes.pager.MeetingMinutesRecordScreen
import com.imhungry.sillok.presentation.screen.meetingminutes.pager.MeetingMinutesSummaryScreen
import com.imhungry.sillok.presentation.viewmodel.meetingminutes.MeetingMinutesViewModel
import com.imhungry.sillok.ui.components.MeetingBasicBox
import com.imhungry.sillok.ui.theme.primaryBackground
import com.imhungry.sillok.ui.theme.whiteBackground
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MeetingMinutesScreen(
    meetingId: Long,
    onBackClick: () -> Unit,
    meetingMinutesViewModel: MeetingMinutesViewModel = hiltViewModel()
) {
    var playbackPosition by remember { mutableStateOf(0L) }
    var currentPosition by remember { mutableStateOf(0f) }
    var isPlaying by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableStateOf(1.0f) }
    val duration = 180f

    BackHandler {
        onBackClick()
    }

    LaunchedEffect(meetingId) {
        meetingMinutesViewModel.initialize(meetingId)
    }

    LaunchedEffect(isPlaying, playbackSpeed) {
        while (isPlaying && currentPosition < duration) {
            delay(100L)
            currentPosition = (currentPosition + 0.1f * playbackSpeed).coerceAtMost(duration)
        }
    }
    val pagerState = rememberPagerState(initialPage = 1)
    val coroutineScope = rememberCoroutineScope()
    var audioBarHeightPx by remember { mutableStateOf(0) }

    MeetingBasicBox(
        navigationBarColor = whiteBackground,
        backgroundColor = primaryBackground
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(bottom = with(LocalDensity.current) { audioBarHeightPx.toDp() })
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                ) {
                    HorizontalPager(
                        count = 3,
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        when (page) {
                            0 -> {
                                MeetingMinutesSummaryScreen(
                                    selectedTab = pagerState.currentPage,
                                    onTabClick = { idx ->
                                        coroutineScope.launch { pagerState.animateScrollToPage(idx) }
                                    },
                                    onTimeClick = { seekMillis ->
                                        playbackPosition = seekMillis
                                        coroutineScope.launch { pagerState.animateScrollToPage(1) }
                                    },
                                    meetingMinutesViewModel = meetingMinutesViewModel
                                )
                            }

                            1 -> MeetingMinutesRecordScreen(
                                meetingId = meetingId,
                                onBackClick = onBackClick,
                                selectedTab = pagerState.currentPage,
                                onTabClick = { idx ->
                                    coroutineScope.launch { pagerState.animateScrollToPage(idx) }
                                },
                                playbackPosition = playbackPosition,
                                isPlaying = isPlaying,
                                onSeekToPosition = { newPosition ->
                                    playbackPosition = newPosition
                                },
                                meetingMinutesViewModel = meetingMinutesViewModel
                            )

                            2 -> MeetingMinutesFeedbackScreen(
                                selectedTab = pagerState.currentPage,
                                onTabClick = { idx ->
                                    coroutineScope.launch { pagerState.animateScrollToPage(idx) }
                                },
                                onTimeClick = { seekMillis ->
                                    playbackPosition = seekMillis
                                    coroutineScope.launch { pagerState.animateScrollToPage(1) }
                                },
                                meetingMinutesViewModel = meetingMinutesViewModel
                            )
                        }
                    }
                    CustomHorizontalPagerIndicator(
                        pagerState = pagerState,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp),
                    )
                }
            }
            AudioPlayerBar(
                meetingMinutesViewModel = meetingMinutesViewModel,
                currentPosition = playbackPosition,
                onPositionChange = { playbackPosition = it },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .onGloballyPositioned { coordinates ->
                        audioBarHeightPx = coordinates.size.height
                    },
                onPlayingChanged = { isPlaying = it },
                onExternalSeek = { seekToMillis ->
                    // AudioPlayerBar 외부에서 seekTo 요청이 왔을 때 처리
                }
            )
        }
    }
}