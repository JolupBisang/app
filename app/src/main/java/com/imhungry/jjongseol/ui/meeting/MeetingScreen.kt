/*package com.imhungry.jjongseol.ui.meeting

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
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
import androidx.compose.runtime.DisposableEffect
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
import com.google.firebase.firestore.FirebaseFirestore
import com.imhungry.jjongseol.R
import com.imhungry.jjongseol.data.model.agenda.AgendaItem
import com.imhungry.jjongseol.data.model.feedback.response.FeedbackListRes
import com.imhungry.jjongseol.data.model.meeting.response.MeetingDetailRes
import com.imhungry.jjongseol.data.model.participationrate.response.ParticipationRateHistoryRes
import com.imhungry.jjongseol.data.model.segment.response.SegmentListRes
import com.imhungry.jjongseol.data.model.summary.response.SummaryListRes
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
import com.imhungry.jjongseol.ui.theme.orange100
import com.imhungry.jjongseol.ui.theme.primaryBackground
import com.imhungry.jjongseol.ui.theme.whiteColor
import com.imhungry.jjongseol.util.DateTimeUtils
import com.imhungry.jjongseol.viewmodel.AgendaViewModel
import com.imhungry.jjongseol.viewmodel.FeedbackViewModel
import com.imhungry.jjongseol.viewmodel.LoginViewModel
import com.imhungry.jjongseol.viewmodel.MeetingViewModel
import com.imhungry.jjongseol.viewmodel.ParticipationRateViewModel
import com.imhungry.jjongseol.viewmodel.SegmentViewModel
import com.imhungry.jjongseol.viewmodel.SummaryViewModel
import com.imhungry.jjongseol.viewmodel.UserViewModel
import kotlinx.coroutines.delay

@Composable
fun MeetingScreen(
    loginViewModel: LoginViewModel = hiltViewModel(),
    meetingViewModel: MeetingViewModel = hiltViewModel(),
    agendaViewModel: AgendaViewModel = hiltViewModel(),
    segmentViewModel: SegmentViewModel = hiltViewModel(),
    participationRateViewModel: ParticipationRateViewModel = hiltViewModel(),
    summaryViewModel: SummaryViewModel = hiltViewModel(),
    //feedbackViewModel: FeedbackViewModel = hiltViewModel(),
    onFinish: (SilRokNavigation) -> Unit,
    navController: NavController,
    meetingId: Long
) {
    val context = LocalContext.current
    val appPrefs = AppPrefs(context)
    val agendas by agendaViewModel.agendaItems.collectAsState()
    val isMeetingLoading by meetingViewModel.isLoading.collectAsState()
    val isAgendaLoading by agendaViewModel.isLoading.collectAsState()
    val meetingDetail by meetingViewModel.meetingDetail.collectAsState()
    val segments by segmentViewModel.segments.collectAsState()
    val participationRates by participationRateViewModel.participationRates.collectAsState()
    //val feedbacks by feedbackViewModel.feedbacks.collectAsState()
    val summaries by summaryViewModel.summaries.collectAsState()
    val meetingError by meetingViewModel.errorMessage.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf<String?>(null) }
    var permissionGranted by remember { mutableStateOf(false) }
    var permissionRequested by remember { mutableStateOf(false) }
    var showLeaveDialog by remember { mutableStateOf(false) }
    val participantInfos by meetingViewModel.participantInfos.collectAsState()
    val meetingNoteCreated by meetingViewModel.meetingNoteCreated.collectAsState()
    val startMillisState = remember { mutableStateOf<Long?>(null) }
    val endMillisState = remember { mutableStateOf<Long?>(null) }
    val startTime by meetingViewModel.meetingStartTime.collectAsState()
    var sseStarted by remember { mutableStateOf(false) }
    // 권한 체크
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

    LaunchedEffect(Unit) {
        permissionGranted = requiredPermissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        if (!permissionGranted && !permissionRequested) {
            launcher.launch(requiredPermissions.toTypedArray())
        }
    }

    // 회의 정보 불러오기
    LaunchedEffect(permissionGranted) {
        meetingViewModel.setTopSheetExpanded(false)
        agendaViewModel.loadAgendas(meetingId)
        meetingViewModel.loadMeetingDetail2(meetingId)
//        segmentViewModel.loadSegments(meetingId, reset = true)
//        participationRateViewModel.loadParticipationRates(meetingId)
//        summaryViewModel.loadSummaries(meetingId, isRecap = true, reset = true)
//        feedbackViewModel.loadFeedbacks(meetingId, reset = true)
//        meetingViewModel.syncMicStateFromServiceOrPrefs(context, meetingId)
    }

    LaunchedEffect(meetingError) {
        dialogMessage = meetingError
        showDialog = dialogMessage != null
    }

    ErrorDialogHandler(
        errorMessage = dialogMessage,
        showDialog = showDialog,
        onFinish = onFinish,
        clearError = {
            meetingViewModel.clearErrorMessage()
            agendaViewModel.clearErrorMessage()
            dialogMessage = null
        },
        loginViewModel = loginViewModel
    )
    val allReady = permissionGranted && !isMeetingLoading && !isAgendaLoading
    // MeetingSseService 시작
    LaunchedEffect(allReady) {
        val isRunning = appPrefs.isMeetingForegroundServiceRunning()
        val runningMeetingId = appPrefs.getRunningMeetingId()
        if (isRunning && runningMeetingId == meetingId) {
        } else {
            if (isRunning) {
                context.stopService(Intent(context, MeetingSseService::class.java))
            }
            Log.d("MeetingStart", "포그라운드 서비스 시작")
            context.startForegroundService(
                Intent(context, MeetingSseService::class.java).apply {
                    putExtra("meetingId", meetingId)
                }
            )
        }
    }
//    DisposableEffect(meetingId) {
//        val db = FirebaseFirestore.getInstance()
//        val meetingRef = db.collection("meetings").document(meetingId.toString())
//        val listenerRegistration = meetingRef.addSnapshotListener { document, error ->
//            if (error != null) {
//                Log.e("Firestore", "Meeting snapshot listener error: ${error.message}")
//                return@addSnapshotListener
//            }
//            if (document != null && document.exists()) {
//                val start = document.getString("startTime")
//                val targetTime = meetingDetail?.targetTime ?: 60
//                if (start != null) {
//                    val startMillis = DateTimeUtils.isoToMillis(start)
//                    val endMillis = startMillis + targetTime * 60_000L
//                    startMillisState.value = startMillis
//                    endMillisState.value = endMillis
//                }
//            }
//        }
//        onDispose { listenerRegistration.remove() }
//    }

    val savedStartTime = startTime
    val targetTimeMinutes = meetingDetail?.targetTime ?: 0
    val savedEndTime = if (savedStartTime != null && targetTimeMinutes > 0) {
        savedStartTime + targetTimeMinutes * 60_000L
    } else null

    val timeText = rememberMeetingElapsedTime(savedStartTime)
    val remainingTime = rememberMeetingRemainingTime(savedEndTime)
    //val timeText = rememberMeetingElapsedTime(startMillisState.value)
    //val remainingTime = rememberMeetingRemainingTime(endMillisState.value)

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

    SetNavigationBarColor(primaryBackground)

    MeetingScreenContent(
        meetingViewModel = meetingViewModel,
        agendaViewModel = agendaViewModel,
        onFinish = { onFinish(it) },
        navController = navController,
        viewModel = meetingViewModel,
        meetingId = meetingId,
        timeText = timeText,
        remainingTime = remainingTime,
        context = context,
        participantInfos = participantInfos,
        startTime = startMillisState.value,
        agendas = agendas,
        meetingDetail = meetingDetail,
        segments = segments,
        participationRates = participationRates,
        //feedbacks = feedbacks,
        summaries = summaries
    )
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
    participantInfos: List<UserInfoResponse>,
    startTime: Long?,
    agendas: List<AgendaItem>,
    meetingDetail: MeetingDetailRes?,
    segments: List<SegmentListRes>,
    participationRates: List<ParticipationRateHistoryRes.UserParticipationRate>,
    //feedbacks: List<FeedbackListRes>,
    summaries: List<SummaryListRes>
) {
    val feedbackList by meetingViewModel.feedbackList.collectAsState()
    val pagerState = rememberPagerState(initialPage = 1)
    var hasUnreadFeedback by remember { mutableStateOf(false) }
    var prevFeedbackCount by remember { mutableStateOf(feedbackList.size) }

    // 새 피드백이 오면 unread 표시
    LaunchedEffect(feedbackList) {
        if (pagerState.currentPage != 2 && feedbackList.size > prevFeedbackCount) {
            hasUnreadFeedback = true
        }
        prevFeedbackCount = feedbackList.size
    }

    // 피드백 페이지로 이동하면 unread 해제
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage == 2) {
            hasUnreadFeedback = false
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
                        participantInfos = participantInfos,
                        startTime = startTime
                    )
                    1 -> MeetingRecordScreen(
                        meetingViewModel = meetingViewModel,
                        agendaViewModel = agendaViewModel,
                        meetingId = meetingId,
                        navController = navController,
                        participantInfos = participantInfos,
                        startTime = startTime
                    )
                    2 -> MeetingFeedbackScreen(
                        meetingViewModel = meetingViewModel,
                        //feedbacks = feedbacks,
                        startTime = startTime,
                        meetingId = meetingId
                    )
                }
            }
            CustomHorizontalPagerIndicator(
                pagerState = pagerState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 4.dp),
                unreadFeedback = hasUnreadFeedback
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
    paddingVertical: Int = 7,
    unreadFeedback: Boolean = false,
    unreadIndicatorColor: Color = orange100
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
*/
package com.imhungry.jjongseol.ui.meeting

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
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
import com.imhungry.jjongseol.data.model.feedback.response.FeedbackListRes
import com.imhungry.jjongseol.data.model.participationrate.response.ParticipationRateHistoryRes
import com.imhungry.jjongseol.data.model.segment.response.SegmentListRes
import com.imhungry.jjongseol.data.model.summary.response.SummaryListRes
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
import com.imhungry.jjongseol.ui.theme.orange100
import com.imhungry.jjongseol.ui.theme.primaryBackground
import com.imhungry.jjongseol.ui.theme.whiteColor
import com.imhungry.jjongseol.viewmodel.AgendaViewModel
import com.imhungry.jjongseol.viewmodel.FeedbackViewModel
import com.imhungry.jjongseol.viewmodel.LoginViewModel
import com.imhungry.jjongseol.viewmodel.MeetingViewModel
import com.imhungry.jjongseol.viewmodel.ParticipationRateViewModel
import com.imhungry.jjongseol.viewmodel.SegmentViewModel
import com.imhungry.jjongseol.viewmodel.SummaryViewModel
import com.imhungry.jjongseol.viewmodel.UserViewModel
import kotlinx.coroutines.delay

@Composable
fun MeetingScreen(
    loginViewModel: LoginViewModel = hiltViewModel(),
    meetingViewModel: MeetingViewModel,
    agendaViewModel: AgendaViewModel,
    segmentViewModel: SegmentViewModel = hiltViewModel(),
    participationRateViewModel: ParticipationRateViewModel = hiltViewModel(),
    summaryViewModel: SummaryViewModel = hiltViewModel(),
    feedbackViewModel: FeedbackViewModel = hiltViewModel(),
    onFinish: (SilRokNavigation) -> Unit,
    navController: NavController,
    meetingId: Long
) {
    val context = LocalContext.current
    val appPrefs = AppPrefs(context)
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
    var permissionGranted by remember { mutableStateOf(false) }
    var permissionRequested by remember { mutableStateOf(false) }
    var sseStarted by remember { mutableStateOf(false) }
    var showLeaveDialog by remember { mutableStateOf(false) }
    val participantInfos by meetingViewModel.participantInfos.collectAsState()
    val startTime by meetingViewModel.meetingStartTime.collectAsState()
    val meetingNoteCreated by meetingViewModel.meetingNoteCreated.collectAsState()
    val segments by segmentViewModel.segments.collectAsState()
    val participationRates by participationRateViewModel.participationRates.collectAsState()
    val feedbackList by feedbackViewModel.feedbacks.collectAsState()
    val summaries by summaryViewModel.summaries.collectAsState()

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
            Log.d("MeetingStart", "회의 정보 불러옴")
        }
    }

    LaunchedEffect(meetingId) {
        meetingViewModel.syncMicStateFromServiceOrPrefs(context, meetingId)
    }

    LaunchedEffect(meetingDetail) {
        if (meetingDetail != null) {
            agendaViewModel.loadAgendas(meetingId)
            segmentViewModel.loadAllSegments(meetingId)
            participationRateViewModel.loadParticipationRates(meetingId)
            summaryViewModel.loadAllSummaries(meetingId, isRecap = true)
            feedbackViewModel.loadAllFeedbacks(meetingId)
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
            agendaViewModel.clearErrorMessage()
            dialogMessage = null
        },
        loginViewModel = loginViewModel
    )

    val allReady = permissionGranted && !isMeetingLoading && !isAgendaLoading

    // 4. MeetingSseService 시작
    LaunchedEffect(allReady) {


        sseStarted = true
        val isRunning = appPrefs.isMeetingForegroundServiceRunning()
        val runningMeetingId = appPrefs.getRunningMeetingId()
        if (isRunning && runningMeetingId == meetingId) {
        } else {
            if (isRunning) {
                context.stopService(Intent(context, MeetingSseService::class.java))
            }
            context.startForegroundService(
                Intent(context, MeetingSseService::class.java).apply {
                    putExtra("meetingId", meetingId)
            }
        )
            Log.d("MeetingStart", "포그라운드 서비스 시작")

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
            && sseStarted

    SetNavigationBarColor(primaryBackground)

//    if (!fullyReady) {
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(primaryBackground),
//            contentAlignment = Alignment.Center
//        ) {
//            CircularProgressIndicator(color = Color(0xFF969696))
//        }
//    } else {
//        MeetingScreenContent(
//            meetingViewModel = meetingViewModel,
//            agendaViewModel = agendaViewModel,
//            onFinish = {
//                sseStarted = false
//                onFinish(it)
//            },
//            navController = navController,
//            viewModel = meetingViewModel,
//            meetingId = meetingId,
//            timeText = timeText,
//            remainingTime = remainingTime,
//            context = context,
//            participantInfos = participantInfos,
//            startTime = savedStartTime
//        )
//    }
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
        participantInfos = participantInfos,
        startTime = savedStartTime,
        segments = segments,
        summaries = summaries,
        usrParticipationRates = participationRates,
        feedbacks = feedbackList
    )
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
    participantInfos: List<UserInfoResponse>,
    startTime: Long?,
    feedbacks: List<FeedbackListRes>,
    segments: List<SegmentListRes>,
    summaries: List<SummaryListRes>,
    usrParticipationRates: List<ParticipationRateHistoryRes.UserParticipationRate>
) {
    val feedbackList by meetingViewModel.feedbackList.collectAsState()
    val pagerState = rememberPagerState(initialPage = 1)
    var hasUnreadFeedback by remember { mutableStateOf(false) }
    var prevFeedbackCount by remember { mutableStateOf(feedbackList.size) }

    // 새 피드백이 오면 unread 표시
    LaunchedEffect(feedbackList) {
        if (pagerState.currentPage != 2 && feedbackList.size > prevFeedbackCount) {
            hasUnreadFeedback = true
        }
        prevFeedbackCount = feedbackList.size
    }

    // 피드백 페이지로 이동하면 unread 해제
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage == 2) {
            hasUnreadFeedback = false
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
                        participantInfos = participantInfos,
                        startTime = startTime,
                        summaries = summaries,
                        usrParticipationRates = usrParticipationRates
                    )
                    1 -> MeetingRecordScreen(
                        meetingViewModel = meetingViewModel,
                        agendaViewModel = agendaViewModel,
                        meetingId = meetingId,
                        navController = navController,
                        participantInfos = participantInfos,
                        startTime = startTime,
                        segments = segments
                    )
                    2 -> MeetingFeedbackScreen(
                        meetingViewModel = meetingViewModel,
                        meetingId = meetingId,
                        startTime = startTime,
                        feedbacks = feedbacks
                    )
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
