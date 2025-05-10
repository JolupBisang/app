package com.imhungry.jjongseol.ui.meeting

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import com.imhungry.jjongseol.R
import com.imhungry.jjongseol.ui.SilRokNavigation
import com.imhungry.jjongseol.ui.component.dialog.CustomDialog
import com.imhungry.jjongseol.ui.meeting.bottom.MeetingControlPanel
import com.imhungry.jjongseol.ui.meeting.pager.MeetingFeedbackScreen
import com.imhungry.jjongseol.ui.meeting.pager.MeetingRecordScreen
import com.imhungry.jjongseol.ui.meeting.pager.MeetingSummaryScreen
import com.imhungry.jjongseol.viewmodel.AgendaViewModel
import com.imhungry.jjongseol.viewmodel.MeetingViewModel
import kotlinx.coroutines.delay

@Composable
fun MeetingScreen(
    meetingViewModel: MeetingViewModel = hiltViewModel(),
    agendaViewModel: AgendaViewModel = hiltViewModel(),
    onFinish: (SilRokNavigation) -> Unit,
    meetingId: Long
) {
    val context = LocalContext.current

    val agendaItems by agendaViewModel.agendaItems.collectAsState()
    val checkedStates by agendaViewModel.checkedStates.collectAsState()
    val isAgendaLoading = agendaItems.isEmpty() || checkedStates.size != agendaItems.size
    val errorMessage by meetingViewModel.errorMessage.collectAsState()
    val showDialog = remember { mutableStateOf(false) }
    val timeText by rememberMeetingStartTime()

    LaunchedEffect(Unit) {
        meetingViewModel.streamController.loadMicState()
    }

    LaunchedEffect(meetingId) {
        agendaViewModel.onError = { apiError ->
            meetingViewModel.setError(apiError)
        }
        agendaViewModel.loadAgendas(meetingId)
    }

    if (errorMessage != null) {
        showDialog.value = true
    }

    val isTokenExpired = errorMessage == "TOKEN_EXPIRED"

    if (showDialog.value && errorMessage != null) {
        CustomDialog(
            description = if (isTokenExpired)
                "로그인 정보가 만료되었어요.\n다시 로그인해주세요."
            else errorMessage,
            confirmText = if (isTokenExpired) "로그인 하기" else "홈으로",
            showDismissButton = false,
            onDismissRequest = {},
            onConfirmExit = {
                showDialog.value = false
                meetingViewModel.clearErrorMessage()
                val destination = if (isTokenExpired) SilRokNavigation.Login else SilRokNavigation.Home
                onFinish(destination)
            }
        )
    }

    PermissionHandler {
        MeetingInitController(
            allReady = !isAgendaLoading,
            meetingViewModel = meetingViewModel,
            meetingId = meetingId,
            timeProvider = { timeText }
        )

        if (isAgendaLoading) {
            Box(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF86CC3B))
            }
        } else {
            MeetingScreenContent(
                onFinish = onFinish,
                onExitConfirmed = {
                    meetingViewModel.streamController.stopStreaming()
                    meetingViewModel.streamController.stopSendingTestData()
                    meetingViewModel.sseSubscriber.stopSse()
                },
                viewModel = meetingViewModel,
                meetingId = meetingId,
                timeText = timeText,
            )
        }
    }
}

@Composable
private fun MeetingInitController(
    allReady: Boolean,
    meetingViewModel: MeetingViewModel,
    meetingId: Long,
    timeProvider: () -> String
) {
    val context = LocalContext.current

    LaunchedEffect(allReady) {
        if (allReady) {
            val prefs = context.getSharedPreferences("meeting_prefs", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("isMeetingOngoing", true).apply()

            meetingViewModel.streamController.apply {
                startStreaming()
                resumeEncoding()
            }
            meetingViewModel.sseSubscriber.apply {
                subscribeToSummary(meetingId, timeProvider)
                subscribeToParticipationRate(meetingId)
                subscribeToFeedback(meetingId, timeProvider)
            }
            meetingViewModel.streamController.startSendingTestData(meetingId, meetingViewModel.viewModelScope)
        }
    }
}

@Composable
private fun rememberMeetingStartTime(): State<String> {
    val context = LocalContext.current
    val startTime = remember {
        val prefs = context.getSharedPreferences("meeting_prefs", Context.MODE_PRIVATE)
        prefs.getLong("meetingStartedAt", System.currentTimeMillis())
    }

    var elapsedSeconds by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            elapsedSeconds = ((System.currentTimeMillis() - startTime) / 1000).toInt()
            delay(1000)
        }
    }

    return remember(elapsedSeconds) {
        derivedStateOf {
            val h = elapsedSeconds / 3600
            val m = (elapsedSeconds % 3600) / 60
            val s = elapsedSeconds % 60
            String.format("%02d:%02d:%02d", h, m, s)
        }
    }
}

@Composable
private fun PermissionHandler(onGranted: @Composable () -> Unit) {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        granted = perms[Manifest.permission.RECORD_AUDIO] == true &&
                (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                        perms[Manifest.permission.POST_NOTIFICATIONS] == true)
    }

    LaunchedEffect(Unit) {
        val requiredPermissions = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requiredPermissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        granted = requiredPermissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

        if (!granted) {
            launcher.launch(requiredPermissions.toTypedArray())
        }
    }

    if (granted) onGranted()
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun MeetingScreenContent(
    onFinish: (SilRokNavigation) -> Unit,
    onExitConfirmed: () -> Unit,
    viewModel: MeetingViewModel,
    meetingId: Long,
    timeText: String
) {
    val pagerState = rememberPagerState(initialPage = 1)

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val (pager, indicator, divider, control) = createRefs()

        HorizontalPager(
            count = 3,
            state = pagerState,
            modifier = Modifier
                .constrainAs(pager) {
                    top.linkTo(parent.top)
                    bottom.linkTo(indicator.top)
                    height = Dimension.fillToConstraints
                }
                .fillMaxWidth()
        ) { page ->
            when (page) {
                0 -> MeetingSummaryScreen()
                1 -> MeetingRecordScreen(meetingId = meetingId)
                2 -> MeetingFeedbackScreen()
            }
        }

        HorizontalPagerIndicator(
            pagerState = pagerState,
            modifier = Modifier
                .padding(top = 12.dp, bottom = 12.dp)
                .constrainAs(indicator) {
                    top.linkTo(pager.bottom)
                    bottom.linkTo(divider.top)
                    centerHorizontallyTo(parent)
                },
            activeColor = Color(0xFF1E93EF),
            inactiveColor = Color.LightGray,
            indicatorWidth = 6.dp,
            spacing = 4.dp
        )

        Divider(
            thickness = 1.dp,
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(divider) {
                    top.linkTo(indicator.bottom)
                    bottom.linkTo(control.top)
                }
        )

        MeetingControlPanel(
            timeText = timeText,
            micIcon = R.drawable.micoff,
            logoutIcon = R.drawable.logout,
            powerIcon = R.drawable.power,
            onFinish = onFinish,
            onExitConfirmed = onExitConfirmed,
            viewModel = viewModel,
            modifier = Modifier
                .constrainAs(control) {
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )
    }
}