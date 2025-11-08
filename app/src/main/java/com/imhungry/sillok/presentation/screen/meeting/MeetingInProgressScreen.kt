package com.imhungry.sillok.presentation.screen.meeting

import android.os.Build
import android.util.Log
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
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.request.ImageRequest
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.imhungry.sillok.R
import com.imhungry.sillok.presentation.permission.PermissionHandler
import com.imhungry.sillok.presentation.screen.meeting.component.MeetingControlPanel
import com.imhungry.sillok.presentation.screen.meeting.pager.MeetingFeedbackScreen
import com.imhungry.sillok.presentation.screen.meeting.pager.MeetingRecordScreen
import com.imhungry.sillok.presentation.screen.meeting.pager.MeetingSummaryScreen
import com.imhungry.sillok.presentation.viewmodel.meeting.MeetingInProgressViewModel
import com.imhungry.sillok.presentation.state.meeting.MeetingInProgressEvent
import com.imhungry.sillok.ui.components.MeetingBasicBox
import com.imhungry.sillok.ui.components.SillokDialog
import com.imhungry.sillok.ui.theme.dialogBackGround
import com.imhungry.sillok.ui.theme.green300
import com.imhungry.sillok.ui.theme.orange100
import com.imhungry.sillok.ui.theme.pagerIndicatorBackground
import com.imhungry.sillok.ui.theme.primaryBackground
import com.imhungry.sillok.ui.theme.whiteBackground
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalPagerApi::class)
@Composable
fun MeetingInProgressScreen(
    meetingId: Long,
    onStopMeeting: () -> Unit,
    onCompleteMeeting: () -> Unit,
    meetingInProgressViewModel: MeetingInProgressViewModel = hiltViewModel()
) {
    var showPermissionRequest by remember { mutableStateOf(true) }

    if (showPermissionRequest) {
        PermissionHandler(
            onPermissionsGranted = {
                showPermissionRequest = false
                // 권한 획득 후 로직
            },
            onPermissionsDenied = { deniedPermissions ->
                // 권한 거부 처리
                Log.w("Permission", "거부된 권한: $deniedPermissions")
            }
        )
    }

    LaunchedEffect(Unit) {
        meetingInProgressViewModel.initialize(meetingId)
    }

    // ViewModel 이벤트 관찰
    LaunchedEffect(Unit) {
        meetingInProgressViewModel.events.collectLatest { event ->
            when (event) {
                is MeetingInProgressEvent.NavigateToHome -> {
                    onCompleteMeeting()
                }
            }
        }
    }

    val pagerState = rememberPagerState(initialPage = 1)
    var showCompleteDialog by remember { mutableStateOf(false) }
    var showLeaveDialog by remember { mutableStateOf(false) }
    
    // ViewModel state 관찰
    val state by meetingInProgressViewModel.state.collectAsState()
    var timeText by remember { mutableStateOf("00:00:00") }
    var remainingTimeText by remember { mutableStateOf("00:00:00") }
    
    // 읽지 않은 피드백 확인
    val hasUnreadFeedback = state.feedbacks.any { !it.isRead }
    
    // 실시간 시간 업데이트
    LaunchedEffect(state.startTime, state.targetTime) {
        if (state.startTime > 0 && state.targetTime > 0) {
            while (coroutineContext.isActive) {
                val currentTime = System.currentTimeMillis()
                val elapsedMillis = currentTime - state.startTime
                val elapsedSeconds = (elapsedMillis / 1000).coerceAtLeast(0)
                
                // 경과 시간 포맷팅 (HH:MM:SS)
                val h = elapsedSeconds / 3600
                val m = (elapsedSeconds % 3600) / 60
                val s = elapsedSeconds % 60
                timeText = String.format("%02d:%02d:%02d", h, m, s)
                
                // 남은 시간 계산
                val targetMillis = state.targetTime * 60 * 1000L
                val remainingMillis = (targetMillis - elapsedMillis).coerceAtLeast(0)
                val remainingSeconds = (remainingMillis / 1000).coerceAtLeast(0)
                
                // 남은 시간 포맷팅 (HH:MM:SS)
                val rh = remainingSeconds / 3600
                val rm = (remainingSeconds % 3600) / 60
                val rs = remainingSeconds % 60
                remainingTimeText = String.format("%02d:%02d:%02d", rh, rm, rs)
                
                delay(1000) // 1초마다 업데이트
            }
        }
    }

    // 시스템 뒤로 가기 버튼 처리
    BackHandler(enabled = true) {
        showLeaveDialog = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MeetingBasicBox(
            navigationBarColor = whiteBackground,
            backgroundColor = primaryBackground,
            isLoading = state.isLoading && !showCompleteDialog && !showLeaveDialog
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
                            1 -> MeetingRecordScreen(
                                meetingId = meetingId,
                                meetingInProgressViewModel = meetingInProgressViewModel,
                                onBackClick = { showLeaveDialog = true }
                            )
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
                    timeText = timeText,
                    remainingTimeText = remainingTimeText,
                    onBack = { showLeaveDialog = true },
                    onComplete = { showCompleteDialog = true },
                    meetingInProgressViewModel = meetingInProgressViewModel
                )
            }
        }

        SillokDialog(
            visible = showCompleteDialog,
            message = "회의를 종료하시겠습니까?",
            confirmText = "예",
            cancelText = "취소",
            onConfirm = {
                // 회의 상태를 COMPLETED로 변경
                meetingInProgressViewModel.completeMeeting()
                //onCompleteMeeting()
                showCompleteDialog = false
            },
            onDismiss = {
                showCompleteDialog = false
            }
        )

        SillokDialog(
            visible = showLeaveDialog,
            message = "회의를 떠나시겠습니까?",
            confirmText = "예",
            cancelText = "취소",
            onConfirm = {
                // 웹소켓과 SSE 연결 정리
                meetingInProgressViewModel.disconnectAll()
                onStopMeeting()
            },
            onDismiss = {
                showLeaveDialog = false
            }
        )
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun CustomHorizontalPagerIndicator(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    pageCount: Int = pagerState.pageCount,
    activeColor: Color = green300,
    inactiveColor: Color = whiteBackground,
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
