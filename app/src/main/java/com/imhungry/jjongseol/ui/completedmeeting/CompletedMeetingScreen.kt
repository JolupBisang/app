package com.imhungry.jjongseol.ui.completedmeeting

import CompletedMeetingRecordScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import com.imhungry.jjongseol.data.model.user.response.UserInfoResponse
import com.imhungry.jjongseol.data.network.config.AppPrefs
import com.imhungry.jjongseol.ui.completedmeeting.component.AudioPlayerBar
import com.imhungry.jjongseol.ui.completedmeeting.pager.CompletedMeetingFeedbackScreen
import com.imhungry.jjongseol.ui.completedmeeting.pager.CompletedMeetingSummaryScreen
import com.imhungry.jjongseol.ui.meeting.CustomHorizontalPagerIndicator
import com.imhungry.jjongseol.ui.meeting.pager.MeetingFeedbackScreen
import com.imhungry.jjongseol.ui.theme.SetNavigationBarColor
import com.imhungry.jjongseol.ui.theme.primaryBackground
import com.imhungry.jjongseol.ui.theme.whiteColor
import com.imhungry.jjongseol.viewmodel.AudioViewModel
import com.imhungry.jjongseol.viewmodel.CompletedMeetingViewModel
import com.imhungry.jjongseol.viewmodel.MeetingViewModel
import kotlinx.coroutines.delay

@Composable
fun CompletedMeetingScreen(
    navController: NavController,
    viewModel: CompletedMeetingViewModel = hiltViewModel(),
    meetingId: Long
) {
    val context = LocalContext.current
    SetNavigationBarColor(whiteColor)

    CompletedMeetingContent(
        navController,
        meetingId = meetingId
    )
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun CompletedMeetingContent(
    navController: NavController,
    meetingViewModel: MeetingViewModel = hiltViewModel(),
    viewModel: CompletedMeetingViewModel = hiltViewModel(),
    audioViewModel: AudioViewModel = hiltViewModel(),
    meetingId: Long
) {
    val context = LocalContext.current

    val audioList by audioViewModel.audioList.collectAsState()
    val errorMessage by audioViewModel.errorMessage.collectAsState()

    LaunchedEffect(meetingId) {
        audioViewModel.loadAudioList(meetingId)
    }

    val appPrefs = remember { AppPrefs(context) }
    val myProfile: UserInfoResponse? = appPrefs.loadMyProfile()
    val id: Long? = myProfile?.id
    val myAudioUrl = audioList.firstOrNull { it.userId == id }?.presignedUrl

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
            .background(primaryBackground)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            val pagerState = rememberPagerState(initialPage = 1)
            HorizontalPager(
                count = 3,
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> CompletedMeetingSummaryScreen()
                    1 -> CompletedMeetingRecordScreen(meetingId = meetingId, navController = navController)
                    2 -> CompletedMeetingFeedbackScreen(meetingId = meetingId)
                }
            }
            CustomHorizontalPagerIndicator(
                pagerState = pagerState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp),
            )
        }

        if (!myAudioUrl.isNullOrEmpty()) {
            AudioPlayerBar(
                audioUrl = myAudioUrl,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            )
        }
    }
}
