package com.imhungry.sillok.presentation.screen.meeting.pager

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.imhungry.sillok.presentation.screen.meeting.component.ChatBubble
import com.imhungry.sillok.presentation.screen.meeting.component.CheckItem
import com.imhungry.sillok.presentation.screen.meeting.component.Notification
import com.imhungry.sillok.presentation.screen.meeting.component.TopSheet
import com.imhungry.sillok.presentation.state.meeting.FeedbackUi
import com.imhungry.sillok.presentation.util.DateTimeUtils
import com.imhungry.sillok.presentation.viewmodel.meeting.AgendaViewModel
import com.imhungry.sillok.presentation.viewmodel.meeting.MeetingInProgressViewModel
import com.imhungry.sillok.ui.components.ScreenHeader
import com.imhungry.sillok.ui.components.SillokInfoDialog
import com.imhungry.sillok.ui.theme.gray400
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalMaterialApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MeetingRecordScreen(
    meetingId: Long,
    meetingInProgressViewModel: MeetingInProgressViewModel,
    onBackClick: () -> Unit,
    agendaViewModel: AgendaViewModel = hiltViewModel()
) {
    val state by meetingInProgressViewModel.state.collectAsState()

    val agendas = state.agendas
    val segments = state.segments
    val feedbacks = state.feedbacks
    val isHost = state.isHost
    val scheduledFeedback by meetingInProgressViewModel.scheduledFeedback.collectAsState()
    val restBreakPeriods by meetingInProgressViewModel.restBreakPeriods.collectAsState()
    val isTopSheetExpanded by agendaViewModel.isTopSheetExpanded
    val firstUncheckedIndex = agendas.indexOfFirst { !it.isCompleted }
    val peekIndex = if (firstUncheckedIndex == -1) agendas.lastIndex else firstUncheckedIndex
    val listState = rememberLazyListState()
    val topSheetHeightPx = remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    var autoScrollEnabled by remember { mutableStateOf(true) }
    val NEAR_BOTTOM_THRESHOLD = 6 // 마지막 아이템에서 6개 위까지는 자동 스크롤 허용
    
    // Pull-to-Refresh 상태
    var isRefreshing by remember { mutableStateOf(false) }
    
    // 새로고침 전 첫 번째 보이는 아이템의 order 저장 (스크롤 위치 유지용)
    var firstVisibleOrderBeforeRefresh by remember { mutableStateOf<Int?>(null) }
    
    // 호스트가 아닐 때 표시할 다이얼로그 상태
    var showHostOnlyDialog by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            coroutineScope.launch {
                // 새로고침 전에 현재 화면에 보이는 첫 번째 아이템의 order 저장
                val firstVisibleIndex = listState.firstVisibleItemIndex
                firstVisibleOrderBeforeRefresh = segments.getOrNull(firstVisibleIndex)?.order

                isRefreshing = true
                try {
                    meetingInProgressViewModel.loadPreviousSegments()
                } finally {
                    isRefreshing = false
                }
            }
        }
    )

    // 표시할 피드백 추적
    var displayedFeedback by remember { mutableStateOf<FeedbackUi?>(null) }
    var showNotification by remember { mutableStateOf(false) }

    LaunchedEffect(listState.firstVisibleItemIndex, segments.size) {
        if (segments.isEmpty()) {
            autoScrollEnabled = true
            return@LaunchedEffect
        }
        
        // 새로고침 중이거나 스크롤 위치 복원 중일 때는 계산하지 않음
        if (isRefreshing || firstVisibleOrderBeforeRefresh != null) {
            return@LaunchedEffect
        }
        
        val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
        val lastItemIndex = segments.lastIndex
        
        // 마지막에서 6개 이내에 있으면 자동 스크롤 활성화
        val distanceFromBottom = lastItemIndex - lastVisibleIndex
        autoScrollEnabled = distanceFromBottom <= NEAR_BOTTOM_THRESHOLD
    }

    // 새로고침 후 스크롤 위치 복원
    LaunchedEffect(segments.size, isRefreshing) {
        if (!isRefreshing && firstVisibleOrderBeforeRefresh != null && segments.isNotEmpty()) {
            // 새로고침이 완료되고 저장된 order 있으면 해당 아이템으로 스크롤
            val targetIndex = segments.indexOfFirst { it.order == firstVisibleOrderBeforeRefresh }
            if (targetIndex >= 0) {
                listState.scrollToItem(targetIndex)
            }
            // 복원 후 초기화 (다음 LaunchedEffect에서 autoScrollEnabled 재계산)
            firstVisibleOrderBeforeRefresh = null
        }
    }

    LaunchedEffect(segments.size) {
        // 새로고침 중이 아니고 자동 스크롤이 활성화되어 있을 때만 맨 아래로 스크롤
        if (segments.isNotEmpty() && autoScrollEnabled && !isRefreshing && firstVisibleOrderBeforeRefresh == null) {
            listState.animateScrollToItem(segments.lastIndex)
        }
    }

    // 새로운 피드백이 올 때마다 알림 표시
    LaunchedEffect(feedbacks) {
        if (feedbacks.isNotEmpty()) {
            val latestUnreadFeedback = feedbacks.lastOrNull { !it.isRead }

            if (latestUnreadFeedback != null) {
                val isNewFeedback = displayedFeedback == null ||
                        (latestUnreadFeedback.comment != displayedFeedback!!.comment ||
                                latestUnreadFeedback.timestamp != displayedFeedback!!.timestamp)

                if (isNewFeedback) {
                    displayedFeedback = latestUnreadFeedback
                    showNotification = true
                }
            }
        }
    }

    // 스케줄링된 피드백 (휴식 시간, 종료 시간 알림) 감시
    LaunchedEffect(scheduledFeedback) {
        if (scheduledFeedback != null) {
            val isNewScheduledFeedback = displayedFeedback == null ||
                    (scheduledFeedback!!.comment != displayedFeedback!!.comment ||
                            scheduledFeedback!!.timestamp != displayedFeedback!!.timestamp)

            if (isNewScheduledFeedback) {
                displayedFeedback = scheduledFeedback
                showNotification = true
            }
        }
    }

    // 알림이 표시되면 4초 후 자동으로 닫기
    LaunchedEffect(showNotification, displayedFeedback) {
        if (showNotification && displayedFeedback != null) {
            delay(4000)

            val feedbackIndex = feedbacks.indexOfLast {
                it.comment == displayedFeedback!!.comment &&
                        it.timestamp == displayedFeedback!!.timestamp
            }
            if (feedbackIndex >= 0) {
                meetingInProgressViewModel.markFeedbackReadAt(feedbackIndex)
            }

            if (displayedFeedback == scheduledFeedback) {
                meetingInProgressViewModel.dismissScheduledFeedback()
            }

            showNotification = false
            displayedFeedback = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            ScreenHeader(
                title = "회의중",
                onBackClick = onBackClick,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(top = 20.dp, start = 20.dp, end = 20.dp)
            )

            if (agendas.isNotEmpty() && peekIndex in agendas.indices) {
                TopSheet(
                    expanded = isTopSheetExpanded,
                    onExpandedChange = { agendaViewModel.setTopSheetExpanded(it) },
                    peekContent = {
                        CheckItem(
                            text = agendas[peekIndex].content,
                            checked = agendas[peekIndex].isCompleted,
                            isFocused = !agendas[peekIndex].isCompleted,
                            onToggle = {
                                if (isHost) {
                                    meetingInProgressViewModel.changeAgendaStatus(
                                        meetingId,
                                        agendas[peekIndex].agendaId,
                                        !agendas[peekIndex].isCompleted
                                    )
                                } else {
                                    showHostOnlyDialog = true
                                }
                            }
                        )
                    },
                    content = {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 161.dp)
                        ) {
                            itemsIndexed(agendas) { i, item ->
                                CheckItem(
                                    text = item.content,
                                    checked = item.isCompleted,
                                    isFocused = !item.isCompleted && firstUncheckedIndex == i,
                                    onToggle = {
                                        if (isHost) {
                                            meetingInProgressViewModel.changeAgendaStatus(
                                                meetingId,
                                                item.agendaId,
                                                !item.isCompleted
                                            )
                                        } else {
                                            showHostOnlyDialog = true
                                        }
                                    }
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coordinates ->
                            topSheetHeightPx.value = coordinates.size.height
                        }
                        .padding(top = 20.dp, start = 28.dp, end = 20.dp, bottom = 4.dp)
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .pullRefresh(pullRefreshState)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                itemsIndexed(segments) { index, message ->
                    if (message.text.isNotBlank()) {
                        if (index == 0) {
                            Spacer(modifier = Modifier.padding(top = 4.dp))
                        }

                        // 마지막으로 타임스탬프를 표시한 메시지의 인덱스 찾기
                        val lastTimestampIndex = remember(segments, index) {
                            var lastIndex = 0 // 첫 번째 메시지는 항상 타임스탬프 표시
                            for (i in 1 until index) {
                                val seg = segments[i]
                                val lastTimestampSeg = segments[lastIndex]
                                    val currentMillis = DateTimeUtils.timeStringToMillis(seg.timestamp) ?: 0L
                                    val lastTimestampMillis = DateTimeUtils.timeStringToMillis(lastTimestampSeg.timestamp) ?: 0L
                                if (currentMillis > 0 && lastTimestampMillis > 0) {
                                    val diffSeconds = (currentMillis - lastTimestampMillis) / 1000
                                    // 마지막 타임스탬프 표시 메시지와 3초 이상 차이나면 타임스탬프 표시
                                    if (diffSeconds >= 3) {
                                        lastIndex = i
                                    }
                                }
                            }
                            lastIndex
                        }

                        // 현재 메시지와 마지막 타임스탬프 표시 메시지와의 시간 차이 계산
                        val shouldShowTimestamp = remember(segments, index, lastTimestampIndex) {
                            if (index == 0) {
                                true // 첫 번째 메시지는 항상 표시
                            } else {
                                val currentMillis = DateTimeUtils.timeStringToMillis(message.timestamp) ?: 0L
                                val lastTimestampMillis = DateTimeUtils.timeStringToMillis(segments[lastTimestampIndex].timestamp) ?: 0L
                                if (currentMillis > 0 && lastTimestampMillis > 0) {
                                    (currentMillis - lastTimestampMillis) / 1000 >= 3
                                } else {
                                    false
                                }
                            }
                        }

                        // 쉬는 시간이 시작되는 시점인지 확인
                        // 세그먼트 존재 여부와 관계없이 쉬는 시간 시작 시점에 DividerWithText 표시
                        val shouldShowRestBreakDivider = remember(segments, index, restBreakPeriods) {
                            if (restBreakPeriods.isEmpty()) {
                                false
                            } else {
                                val currentTimestampSeconds = timestampToSeconds(message.timestamp)
                                
                                // 각 쉬는 시간 구간의 시작 시점과 비교
                                restBreakPeriods.any { (startTime, _) ->
                                    val startSeconds = timestampToSeconds(startTime)
                                    
                                    if (index == 0) {
                                        // 첫 번째 세그먼트가 쉬는 시간 시작 시점 이후면 표시
                                        currentTimestampSeconds >= startSeconds
                                    } else {
                                        // 이전 세그먼트는 쉬는 시간 시작 시점 이전이고,
                                        // 현재 세그먼트가 쉬는 시간 시작 시점 이후면 표시
                                        val previousTimestampSeconds = timestampToSeconds(segments[index - 1].timestamp)
                                        previousTimestampSeconds < startSeconds && currentTimestampSeconds >= startSeconds
                                    }
                                }
                            }
                        }

                        ChatBubble(
                            segment = message,
                            shouldShowTimestamp = shouldShowTimestamp
                        )

                        // 쉬는 시간이 시작될 때만 DividerWithText 표시
                        if (shouldShowRestBreakDivider) {
                            DividerWithText()
                        }

                        if (index == segments.lastIndex) {
                            Spacer(modifier = Modifier.padding(bottom = 28.dp))
                        }
                    }
                }
                }
                
                // Pull-to-Refresh 인디케이터
                PullRefreshIndicator(
                    refreshing = isRefreshing,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }

        if (showNotification && displayedFeedback != null) {
            val isRestBreakNotification = displayedFeedback == scheduledFeedback &&
                    displayedFeedback!!.comment.contains("휴식 시간")

            if (isRestBreakNotification) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(
                            top = with(LocalDensity.current) { topSheetHeightPx.value.toDp() } + 68.dp,
                            start = 20.dp,
                            end = 20.dp
                        )
                ) {
                    Column {
                        SwipeToDismissNotification(
                            feedback = displayedFeedback!!,
                            onDismiss = {
                                val feedbackIndex = feedbacks.indexOfLast {
                                    it.comment == displayedFeedback!!.comment &&
                                            it.timestamp == displayedFeedback!!.timestamp
                                }
                                if (feedbackIndex >= 0) {
                                    meetingInProgressViewModel.markFeedbackReadAt(feedbackIndex)
                                }

                                if (displayedFeedback == scheduledFeedback) {
                                    meetingInProgressViewModel.dismissScheduledFeedback()
                                }

                                showNotification = false
                                displayedFeedback = null
                            }
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(
                            top = with(LocalDensity.current) { topSheetHeightPx.value.toDp() } + 68.dp,
                            start = 20.dp,
                            end = 20.dp
                        )
                ) {
                    SwipeToDismissNotification(
                        feedback = displayedFeedback!!,
                        onDismiss = {
                            val feedbackIndex = feedbacks.indexOfLast {
                                it.comment == displayedFeedback!!.comment &&
                                        it.timestamp == displayedFeedback!!.timestamp
                            }
                            if (feedbackIndex >= 0) {
                                meetingInProgressViewModel.markFeedbackReadAt(feedbackIndex)
                            }

                            if (displayedFeedback == scheduledFeedback) {
                                meetingInProgressViewModel.dismissScheduledFeedback()
                            }

                            showNotification = false
                            displayedFeedback = null
                        }
                    )
                }
            }
        }
        
        // 호스트가 아닐 때 표시할 다이얼로그
        SillokInfoDialog(
            visible = showHostOnlyDialog,
            message = "안건 상태 변경은 호스트만 가능합니다.",
            confirmText = "확인",
            onConfirm = { showHostOnlyDialog = false }
        )
    }
}

@Composable
fun SwipeToDismissNotification(
    feedback: FeedbackUi,
    onDismiss: () -> Unit
) {
    val offsetX = remember { Animatable(0f) }
    val threshold = 200f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .pointerInput(Unit) {
                coroutineScope {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { _, dragAmount ->
                            launch {
                                // 드래그 중 위치 이동
                                val newOffset = offsetX.value + dragAmount
                                offsetX.snapTo(newOffset.coerceIn(-1000f, 1000f))
                            }
                        },
                        onDragEnd = {
                            launch {
                                if (abs(offsetX.value) > threshold) {
                                    val target = if (offsetX.value > 0) 2000f else -2000f

                                    offsetX.animateTo(
                                        target,
                                        animationSpec = androidx.compose.animation.core.tween(
                                            durationMillis = 300
                                        )
                                    )

                                    onDismiss()
                                } else {
                                    offsetX.animateTo(
                                        0f,
                                        animationSpec = androidx.compose.animation.core.spring(
                                            dampingRatio = 0.7f,
                                            stiffness = 300f
                                        )
                                    )
                                }
                            }
                        }
                    )
                }
            }
            .offset { IntOffset(offsetX.value.toInt(), 0) }
    ) {
        Notification(
            feedback = feedback,
            blur = true,
            isRead = true
        )
    }
}

@Composable
fun DividerWithText(
    text: String = "쉬는 시간",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 왼쪽 선
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(gray400)
        )

        // 가운데 텍스트
        Text(
            text = text,
            color = gray400,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Light,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        // 오른쪽 선
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(gray400)
        )
    }
}

/**
 * 세그먼트의 timestamp가 쉬는 시간 범위에 있는지 확인
 */
private fun isInRestBreak(
    timestamp: String,
    restBreakPeriods: List<Pair<String, String>>
): Boolean {
    if (restBreakPeriods.isEmpty()) return false

    val timestampSeconds = timestampToSeconds(timestamp)

    return restBreakPeriods.any { (startTime, endTime) ->
        val startSeconds = timestampToSeconds(startTime)
        val endSeconds = timestampToSeconds(endTime)
        timestampSeconds >= startSeconds && timestampSeconds <= endSeconds
    }
}

/**
 * HH:MM:SS 형식의 timestamp를 초로 변환
 */
private fun timestampToSeconds(timestamp: String): Int {
    val parts = timestamp.split(":").map { it.toIntOrNull() ?: 0 }
    return when (parts.size) {
        3 -> parts[0] * 3600 + parts[1] * 60 + parts[2]
        2 -> parts[0] * 60 + parts[1]
        else -> 0
    }
}
