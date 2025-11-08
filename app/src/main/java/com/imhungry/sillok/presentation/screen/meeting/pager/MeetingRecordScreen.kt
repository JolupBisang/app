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
import androidx.compose.foundation.layout.asPaddingValues
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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
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
import com.imhungry.sillok.ui.theme.gray400
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

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
    val scheduledFeedback by meetingInProgressViewModel.scheduledFeedback.collectAsState()
    val restBreakPeriods by meetingInProgressViewModel.restBreakPeriods.collectAsState()
    val isTopSheetExpanded by agendaViewModel.isTopSheetExpanded
    val firstUncheckedIndex = agendas.indexOfFirst { !it.isCompleted }
    val peekIndex = if (firstUncheckedIndex == -1) agendas.lastIndex else firstUncheckedIndex
    val listState = rememberLazyListState()
    val topSheetHeightPx = remember { mutableStateOf(0) }
    
    // 표시할 피드백 추적
    var displayedFeedback by remember { mutableStateOf<FeedbackUi?>(null) }
    var showNotification by remember { mutableStateOf(false) }
    
    // 새로운 피드백이 올 때마다 알림 표시
    LaunchedEffect(feedbacks) {
        if (feedbacks.isNotEmpty()) {
            // 가장 최신 피드백 찾기 (읽지 않은 것)
            val latestUnreadFeedback = feedbacks.lastOrNull { !it.isRead }
            
            if (latestUnreadFeedback != null) {
                // 새로운 피드백이거나 아직 표시하지 않은 피드백인 경우
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
            // 새로운 스케줄링된 피드백이 오면 알림 표시
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
            delay(4000) // 4초 대기
            
            // 피드백 읽음 처리
            val feedbackIndex = feedbacks.indexOfLast { 
                it.comment == displayedFeedback!!.comment && 
                it.timestamp == displayedFeedback!!.timestamp 
            }
            if (feedbackIndex >= 0) {
                meetingInProgressViewModel.markFeedbackReadAt(feedbackIndex)
            }
            
            // 스케줄링된 피드백인 경우 해제
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
                            onToggle = { meetingInProgressViewModel.changeAgendaStatus(meetingId, agendas[peekIndex].agendaId, !agendas[peekIndex].isCompleted) }
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
                                    onToggle = { meetingInProgressViewModel.changeAgendaStatus(meetingId, item.agendaId, !item.isCompleted) }
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coordinates ->
                            topSheetHeightPx.value = coordinates.size.height
                        }
                        .padding(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 4.dp)
                )

            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                itemsIndexed(segments) { index, message ->
                    if (message.text.isNotBlank()) {
                        if (index == 0) {
                            Spacer(modifier = Modifier.padding(top = 4.dp))
                        }
                        
                        // 이전 세그먼트 확인
                        val prevSegment = if (index > 0) segments[index - 1] else null
                        val isPrevInRestBreak = prevSegment != null && isInRestBreak(prevSegment.timestamp, restBreakPeriods)
                        val isCurrentInRestBreak = isInRestBreak(message.timestamp, restBreakPeriods)
                        
                        // 쉬는 시간 시작
                        if (!isPrevInRestBreak && isCurrentInRestBreak) {
                            DividerWithText()
                        }
                        
                        ChatBubble(segment = message)
                        
                        if (index == segments.lastIndex) {
                            Spacer(modifier = Modifier.padding(bottom = 28.dp))
                        }
                    }
                }
            }

        }
        
        // 새로운 피드백 알림 표시
        if (showNotification && displayedFeedback != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = with(LocalDensity.current) { topSheetHeightPx.value.toDp() } + 20.dp, start = 20.dp, end = 20.dp)
            ) {
                SwipeToDismissNotification(
                    feedback = displayedFeedback!!,
                    onDismiss = {
                        // 피드백 읽음 처리
                        val feedbackIndex = feedbacks.indexOfLast { 
                            it.comment == displayedFeedback!!.comment && 
                            it.timestamp == displayedFeedback!!.timestamp 
                        }
                        if (feedbackIndex >= 0) {
                            meetingInProgressViewModel.markFeedbackReadAt(feedbackIndex)
                        }
                        
                        // 스케줄링된 피드백인 경우 해제
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
                                        animationSpec = androidx.compose.animation.core.tween(durationMillis = 300)
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
private fun isInRestBreak(timestamp: String, restBreakPeriods: List<Pair<String, String>>): Boolean {
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
