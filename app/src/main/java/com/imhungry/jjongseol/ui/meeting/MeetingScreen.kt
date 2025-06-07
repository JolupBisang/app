package com.imhungry.jjongseol.ui.meeting

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.imhungry.jjongseol.R
import com.imhungry.jjongseol.data.model.meeting.MeetingState
import com.imhungry.jjongseol.data.model.meeting.MeetingStatus
import com.imhungry.jjongseol.data.model.user.response.UserInfoResponse
import com.imhungry.jjongseol.data.network.config.AppPrefs
import com.imhungry.jjongseol.service.MeetingSseService
import com.imhungry.jjongseol.ui.SilRokNavigation
import com.imhungry.jjongseol.ui.component.dialog.ErrorDialogHandler
import com.imhungry.jjongseol.ui.meeting.bottom.MeetingControlPanel
import com.imhungry.jjongseol.ui.meeting.bottom.showExitDialog
import com.imhungry.jjongseol.ui.meeting.pager.MeetingFeedbackScreen
import com.imhungry.jjongseol.ui.meeting.pager.MeetingRecordScreen
import com.imhungry.jjongseol.ui.meeting.pager.MeetingSummaryScreen
import com.imhungry.jjongseol.ui.theme.SetNavigationBarColor
import com.imhungry.jjongseol.ui.theme.green200
import com.imhungry.jjongseol.ui.theme.primaryBackground
import com.imhungry.jjongseol.ui.theme.whiteColor
import com.imhungry.jjongseol.viewmodel.AgendaViewModel
import com.imhungry.jjongseol.viewmodel.LoginViewModel
import com.imhungry.jjongseol.viewmodel.MeetingViewModel
import com.imhungry.jjongseol.viewmodel.UserViewModel
import kotlinx.coroutines.delay

@Composable
fun MeetingScreen(
    loginViewModel: LoginViewModel = hiltViewModel(),
    meetingViewModel: MeetingViewModel,
    agendaViewModel: AgendaViewModel,
    userViewModel: UserViewModel = hiltViewModel(),
    onFinish: (SilRokNavigation) -> Unit,
    navController: NavController,
    meetingId: Long
) {
    val agendas by agendaViewModel.agendaItems.collectAsState()
    val agendaError by agendaViewModel.errorMessage.collectAsState()
    val isMeetingLoading by meetingViewModel.isLoading.collectAsState()
    val isAgendaLoading by agendaViewModel.isLoading.collectAsState()
    val isParticipantLoading by meetingViewModel.isParticipantLoading.collectAsState()
    val isStatusUpdating by meetingViewModel.isStatusUpdating.collectAsState()
    val meetingDetail by meetingViewModel.meetingDetail.collectAsState()
    val meetingError by meetingViewModel.errorMessage.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf<String?>(null) }
    val meetingStatus by meetingViewModel.meetingStatus.collectAsState()
    val context = LocalContext.current
    var permissionGranted by remember { mutableStateOf(false) }
    var permissionRequested by remember { mutableStateOf(false) }
    var sseStarted by remember { mutableStateOf(false) }
    var showLeaveDialog by remember { mutableStateOf(false) }
    val participantInfos by meetingViewModel.participantInfos.collectAsState()
    val startTime by meetingViewModel.meetingStartTime.collectAsState()
    val meetingNoteCreated by meetingViewModel.meetingNoteCreated.collectAsState()

    // 1. 권한 체크
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
        permissionGranted = requiredPermissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        if (!permissionGranted && !permissionRequested) {
            launcher.launch(requiredPermissions.toTypedArray())
        }
    }

    // 2. 회의 정보 불러오기
    LaunchedEffect(meetingId, permissionGranted) {
        if (permissionGranted) {
            meetingViewModel.setTopSheetExpanded(false)
            meetingViewModel.loadMeetingDetail2(meetingId)
        }
    }

    // 3. 아젠다 불러오기
    LaunchedEffect(meetingDetail) {
        if (meetingDetail != null) {
            agendaViewModel.loadAgendas(meetingId)
        }
    }

    LaunchedEffect(meetingError, agendaError) {
        dialogMessage = agendaError ?: meetingError
        showDialog = dialogMessage != null
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

    val allReady = permissionGranted && !isMeetingLoading && !isAgendaLoading && !isParticipantLoading

    // 4. MeetingSseService 시작
    LaunchedEffect(allReady) {
        if (allReady && !sseStarted) {
            context.startForegroundService(
                Intent(context, MeetingSseService::class.java).apply {
                    putExtra("meetingId", meetingId)
                }
            )
            sseStarted = true
        }
    }

    // 5. 회의 시작 & 종료 시간 설정
    val savedStartTime = startTime
    val targetTimeMinutes = meetingDetail?.targetTime ?: 0
    val savedEndTime = if (savedStartTime != null && targetTimeMinutes > 0) {
        savedStartTime + targetTimeMinutes * 60_000L
    } else null

    val timeText = rememberMeetingElapsedTime(savedStartTime)
    val remainingTime = rememberMeetingRemainingTime(savedEndTime)

    BackHandler(enabled = true) {
        showLeaveDialog = true
    }

    if (showLeaveDialog) {
        showExitDialog(
            description = "회의를 떠나시겠습니까?",
            confirmText = "예",
            onConfirm = {
                context.stopService(Intent(context, MeetingSseService::class.java))
                onFinish(SilRokNavigation.Home)
                showLeaveDialog = false
            },
            onDismiss = { showLeaveDialog = false }
        )
    }

    LaunchedEffect(meetingNoteCreated) {
        val mid = meetingNoteCreated
        if (mid != null) {
            navController.navigate("meetingRoute/end/$mid") {
                popUpTo(0)
            }
            meetingViewModel.resetMeetingNoteCreated()
        }
    }

    // 준비 상태 체크
    val fullyReady = permissionGranted
            && !isMeetingLoading
            && !isAgendaLoading
            && !isParticipantLoading
            && startTime != null
            && meetingDetail != null

    val shouldShowLoading = isStatusUpdating || !fullyReady

    SetNavigationBarColor(primaryBackground)

    if (shouldShowLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(primaryBackground),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = primaryBackground)
        }
    } else {
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
            context = context,
            participantInfos = participantInfos
        )
    }
}

@Composable
fun rememberMeetingElapsedTime(savedStartTime: Long?): String {
    val elapsedTime = produceState(initialValue = "00:00:00", savedStartTime) {
        while (savedStartTime != null) {
            val elapsed = (System.currentTimeMillis() - savedStartTime) / 1000
            val h = elapsed / 3600
            val m = (elapsed % 3600) / 60
            val s = elapsed % 60
            value = String.format("%02d:%02d:%02d", h, m, s)
            delay(1000)
        }
    }
    return elapsedTime.value
}

@Composable
fun rememberMeetingRemainingTime(savedEndTime: Long?): String {
    val remainingTime = produceState(initialValue = "00:00:00", savedEndTime) {
        while (savedEndTime != null) {
            val remain = ((savedEndTime - System.currentTimeMillis()) / 1000).coerceAtLeast(0)
            val h = remain / 3600
            val m = (remain % 3600) / 60
            val s = remain % 60
            value = String.format("%02d:%02d:%02d", h, m, s)
            delay(1000)
        }
    }
    return remainingTime.value
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
    context: Context,
    participantInfos: List<UserInfoResponse>
) {
    val pagerState = rememberPagerState(initialPage = 1)

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
            HorizontalPager(
                count = 3,
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> MeetingSummaryScreen(
                        meetingViewModel = meetingViewModel,
                        agendaViewModel = agendaViewModel,
                        meetingId = meetingId,
                        participantInfos = participantInfos
                    )
                    1 -> MeetingRecordScreen(
                        meetingViewModel = meetingViewModel,
                        agendaViewModel = agendaViewModel,
                        meetingId = meetingId,
                        navController = navController,
                        participantInfos = participantInfos
                    )
                    2 -> MeetingFeedbackScreen(
                        meetingViewModel = meetingViewModel,
                        meetingId = meetingId
                    )
                }
            }
            CustomHorizontalPagerIndicator(
                pagerState = pagerState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 4.dp)
            )
        }
        MeetingControlPanel(
            timeText = timeText,
            remainingTimeText = remainingTime,
            micIcon = R.drawable.micoff,
            onFinish = onFinish,
            navController = navController,
            viewModel = viewModel,
            modifier = Modifier
                .background(primaryBackground)
                .fillMaxWidth(),
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
    activeColor: Color = green200,
    inactiveColor: Color = whiteColor,
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
