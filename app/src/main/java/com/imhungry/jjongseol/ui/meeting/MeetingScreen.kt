package com.imhungry.jjongseol.ui.meeting

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
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
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import com.imhungry.jjongseol.R
import com.imhungry.jjongseol.ui.meeting.bottom.MeetingControlPanel
import com.imhungry.jjongseol.ui.meeting.pager.MeetingFeedbackScreen
import com.imhungry.jjongseol.ui.meeting.pager.MeetingRecordScreen
import com.imhungry.jjongseol.ui.meeting.pager.MeetingSummaryScreen
import com.imhungry.jjongseol.viewmodel.MeetingViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MeetingScreen(
    viewModel: MeetingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var permissionGranted by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionGranted = isGranted
        if (isGranted) {
            viewModel.startRecordingAndStreaming()
        }
    }

    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            permissionGranted = true
            viewModel.startRecordingAndStreaming()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    MeetingScreenContent()
}

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalPagerApi::class)
@Composable
fun MeetingScreenContent() {

    var elapsedSeconds by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            elapsedSeconds++
        }
    }

    val timeText = remember(elapsedSeconds) {
        val hours = elapsedSeconds / 3600
        val minutes = (elapsedSeconds % 3600) / 60
        val seconds = elapsedSeconds % 60
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
    ) {
        val pagerState = rememberPagerState(initialPage = 1)

        HorizontalPager(
            count = 3,
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.70f)
        ) { page ->
            when (page) {
                0 -> MeetingSummaryScreen()
                1 -> MeetingRecordScreen()
                2 -> MeetingFeedbackScreen()
            }
        }

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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Divider(
                color = Color.LightGray,
                thickness = 1.dp,
                modifier = Modifier.fillMaxWidth()
            )
        }

        MeetingControlPanel(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.20f),
            timeText = "00:03:11",
            micEnabled = true,
            micIcon = R.drawable.micoff,
            logoutIcon = R.drawable.logout,
            powerIcon = R.drawable.power
        )
    }
}
