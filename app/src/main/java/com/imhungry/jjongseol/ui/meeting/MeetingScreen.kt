package com.imhungry.jjongseol.ui.meeting

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
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
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.imhungry.jjongseol.R
import com.imhungry.jjongseol.data.model.meeting.MeetingStatus
import com.imhungry.jjongseol.service.MeetingSseService
import com.imhungry.jjongseol.ui.SilRokNavigation
import com.imhungry.jjongseol.ui.component.dialog.ErrorDialogHandler
import com.imhungry.jjongseol.ui.meeting.bottom.MeetingControlPanel
import com.imhungry.jjongseol.ui.meeting.pager.MeetingFeedbackScreen
import com.imhungry.jjongseol.ui.meeting.pager.MeetingRecordScreen
import com.imhungry.jjongseol.ui.meeting.pager.MeetingSummaryScreen
import com.imhungry.jjongseol.ui.theme.SetNavigationBarColor
import com.imhungry.jjongseol.ui.theme.whiteColor
import com.imhungry.jjongseol.viewmodel.AgendaViewModel
import com.imhungry.jjongseol.viewmodel.LoginViewModel
import com.imhungry.jjongseol.viewmodel.MeetingViewModel
import kotlinx.coroutines.delay

@Composable
fun MeetingScreen(
    loginViewModel: LoginViewModel = hiltViewModel(),
    meetingViewModel: MeetingViewModel,
    agendaViewModel: AgendaViewModel,
    onFinish: (SilRokNavigation) -> Unit,
    navController: NavController,
    meetingId: Long
) {
    val agendas by agendaViewModel.agendaItems.collectAsState()
    val agendaError by agendaViewModel.errorMessage.collectAsState()
    val isMeetingLoading by meetingViewModel.isLoading.collectAsState()
    val isAgendaLoading by agendaViewModel.isLoading.collectAsState()
    val isStatusUpdating by meetingViewModel.isStatusUpdating.collectAsState()
    val meetingDetail by meetingViewModel.meetingDetail.collectAsState()
    val meetingError by meetingViewModel.errorMessage.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf<String?>(null) }
    val meetingStatus by meetingViewModel.meetingStatus.collectAsState()
    val context = LocalContext.current
    var permissionGranted by remember { mutableStateOf(false) }
    var permissionRequested by remember { mutableStateOf(false) }
    var startTimeMillis by remember { mutableStateOf<Long?>(null) }
    var sseStarted by remember { mutableStateOf(false) }

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
        meetingViewModel.loadMeetingDetail2(meetingId)
    }

    LaunchedEffect(meetingDetail) {
        if (meetingDetail != null) {
            agendaViewModel.loadAgendas(meetingId)
        }
    }

    val remainingTime by remember(meetingDetail) {
        mutableStateOf(formatTargetTime(meetingDetail?.targetTime))
    }

    LaunchedEffect(meetingError, agendaError) {
        dialogMessage = meetingError ?: agendaError
        showDialog = dialogMessage != null
    }

    LaunchedEffect(meetingId) {
        permissionGranted = requiredPermissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        if (!permissionGranted && !permissionRequested) {
            launcher.launch(requiredPermissions.toTypedArray())
        }
    }

    ErrorDialogHandler(
        errorMessage = dialogMessage,
        showDialog = showDialog,
        onFinish = onFinish,
        clearError = {
            meetingViewModel.clearErrorMessage()
        },
        loginViewModel = loginViewModel
    )

    val allReady = permissionGranted && !isMeetingLoading && !isAgendaLoading
    val showLoading = isMeetingLoading || isAgendaLoading || isStatusUpdating

    LaunchedEffect(allReady) {
        if (allReady && startTimeMillis == null) {
            startTimeMillis = System.currentTimeMillis()
        }
    }

    LaunchedEffect(meetingStatus) {
        if (meetingStatus == MeetingStatus.COMPLETED) {
            context.stopService(Intent(context, MeetingSseService::class.java))
            sseStarted = false
            navController.navigate("meetingRoute/completed/$meetingId")  {
                popUpTo(0)
            }
        }
    }

    val timeText = rememberMeetingElapsedTime(startTimeMillis)

    if (!allReady || showLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(whiteColor),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF969696))
        }
    } else {
        SetNavigationBarColor(Color(0xFFE5E5E5))
        LaunchedEffect(allReady) {
            if (!sseStarted) {
                context.startForegroundService(
                    Intent(context, MeetingSseService::class.java).apply {
                        putExtra("meetingId", meetingId)
                    }
                )
                sseStarted = true
            }
        }
        MeetingScreenContent(
            meetingViewModel = meetingViewModel,
            agendaViewModel = agendaViewModel,
            onFinish = {
                sseStarted = false
                onFinish(it)
            },
            navController = navController,
            viewModel = meetingViewModel,
            meetingId = meetingId,
            timeText = timeText,
            remainingTime = remainingTime,
            context = context
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
    meetingViewModel: MeetingViewModel,
    agendaViewModel: AgendaViewModel,
    onFinish: (SilRokNavigation) -> Unit,
    navController: NavController,
    viewModel: MeetingViewModel,
    meetingId: Long,
    timeText: String,
    remainingTime: String,
    context: Context
) {
    val pagerState = rememberPagerState(initialPage = 1)

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        val (pager, indicator, control) = createRefs()

        HorizontalPager(
            count = 3,
            state = pagerState,
            modifier = Modifier
                .constrainAs(pager) {
                    top.linkTo(parent.top)
                    bottom.linkTo(control.top)
                    height = Dimension.fillToConstraints
                }
                .fillMaxWidth()
        ) { page ->
            when (page) {
                0 -> MeetingSummaryScreen(
                    meetingViewModel = meetingViewModel,
                    agendaViewModel = agendaViewModel
                )
                1 -> MeetingRecordScreen(
                    meetingViewModel = meetingViewModel,
                    agendaViewModel = agendaViewModel,
                    meetingId = meetingId
                )
                2 -> MeetingFeedbackScreen(meetingViewModel = meetingViewModel)
            }
        }

        CustomHorizontalPagerIndicator(
            pagerState = pagerState,
            modifier = Modifier
                .padding(bottom = 12.dp)
                .constrainAs(indicator) {
                    bottom.linkTo(control.top)
                    centerHorizontallyTo(parent)
                }
        )

        MeetingControlPanel(
            timeText = timeText,
            remainingTimeText = remainingTime,
            micIcon = R.drawable.micoff,
            onFinish = onFinish,
            navController = navController,
            viewModel = viewModel,
            modifier = Modifier
                .background(Color(0xFFE5E5E5))
                .constrainAs(control) {
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            meetingId = meetingId,
            context = context
        )
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun CustomHorizontalPagerIndicator(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    pageCount: Int = pagerState.pageCount,
    activeColor: Color = Color(0xFF0004F8),
    inactiveColor: Color = Color(0xFFF6F6F6),
    backgroundColor: Color = Color(0xFFD9D9D9),
    indicatorSize: Int = 8,
    indicatorSpacing: Int = 7,
    paddingHorizontal: Int = 10,
    paddingVertical: Int = 7
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
                Box(
                    modifier = Modifier
                        .size(indicatorSize.dp)
                        .background(
                            color = if (pagerState.currentPage == index) activeColor else inactiveColor,
                            shape = CircleShape
                        )
                )
            }
        }
    }
}
