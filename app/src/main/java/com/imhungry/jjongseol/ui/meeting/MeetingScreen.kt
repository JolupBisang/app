package com.imhungry.jjongseol.ui.meeting

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import com.imhungry.jjongseol.ui.component.dialog.ErrorDialogHandler
import com.imhungry.jjongseol.ui.meeting.bottom.MeetingControlPanel
import com.imhungry.jjongseol.ui.meeting.pager.MeetingFeedbackScreen
import com.imhungry.jjongseol.ui.meeting.pager.MeetingRecordScreen
import com.imhungry.jjongseol.ui.meeting.pager.MeetingSummaryScreen
import com.imhungry.jjongseol.viewmodel.AgendaViewModel
import com.imhungry.jjongseol.viewmodel.LoginViewModel
import com.imhungry.jjongseol.viewmodel.MeetingViewModel
import kotlinx.coroutines.delay

@Composable
fun MeetingScreen(
    loginViewModel: LoginViewModel = hiltViewModel(),
    meetingViewModel: MeetingViewModel = hiltViewModel(),
    agendaViewModel: AgendaViewModel = hiltViewModel(),
    onFinish: (SilRokNavigation) -> Unit,
    meetingId: Long
) {
    val agendas by agendaViewModel.agendaItems.collectAsState()
    val isAgendaLoading = agendas.isEmpty()
    val errorMessage by meetingViewModel.errorMessage.collectAsState()
    val showDialog = remember { mutableStateOf(false) }

    val context = LocalContext.current
    var permissionGranted by remember { mutableStateOf(false) }
    var permissionRequested by remember { mutableStateOf(false) }
    var startTimeMillis by remember { mutableStateOf<Long?>(null) }

    val requiredPermissions = remember {
        buildList {
            add(Manifest.permission.RECORD_AUDIO)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        permissionGranted = requiredPermissions.all {
            perms[it] == true
        }
        permissionRequested = true
    }

    LaunchedEffect(meetingId) {
        agendaViewModel.loadAgendas(meetingId)
        permissionGranted = requiredPermissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        if (!permissionGranted && !permissionRequested) {
            launcher.launch(requiredPermissions.toTypedArray())
        }
    }

    LaunchedEffect(errorMessage) {
        Log.d("UI", "Error 메시지 변경됨: $errorMessage")
        if (errorMessage != null) {
            showDialog.value = true
        }
    }

    ErrorDialogHandler(
        errorMessage = errorMessage,
        showDialog = showDialog,
        onFinish = onFinish,
        clearError = {
            meetingViewModel.clearErrorMessage()
            meetingViewModel.cleanupSession()
        },
        loginViewModel = loginViewModel
    )

    val allReady = permissionGranted && !isAgendaLoading
    LaunchedEffect(allReady) {
        if (allReady && startTimeMillis == null) {
            startTimeMillis = System.currentTimeMillis()

            meetingViewModel.initializeSession(
                meetingId = meetingId,
                timeProvider = {
                    val elapsed = (System.currentTimeMillis() - (startTimeMillis ?: 0L)) / 1000
                    String.format("%02d:%02d:%02d", elapsed / 3600, (elapsed % 3600) / 60, elapsed % 60)
                },
                scope = meetingViewModel.viewModelScope
            )
        }
    }

    val timeText = rememberMeetingElapsedTime(startTimeMillis)

    if (!allReady) {
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
                meetingViewModel.cleanupSession()
                onFinish(SilRokNavigation.MeetingEnd)
            },
            viewModel = meetingViewModel,
            meetingId = meetingId,
            timeText = timeText
        )
    }
}

@Composable
fun rememberMeetingElapsedTime(startTimeMillis: Long?): String {
    val elapsedTime = produceState(initialValue = "00:00:00", startTimeMillis) {
        while (startTimeMillis != null) {
            val elapsed = (System.currentTimeMillis() - startTimeMillis) / 1000
            val h = elapsed / 3600
            val m = (elapsed % 3600) / 60
            val s = elapsed % 60
            value = String.format("%02d:%02d:%02d", h, m, s)
            delay(1000)
        }
    }
    return elapsedTime.value
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
