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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.imhungry.sillok.ui.theme.gray400
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MeetingRecordScreen(
    meetingInProgressViewModel: MeetingInProgressViewModel,
    agendaViewModel: AgendaViewModel = hiltViewModel()
) {
    val state by meetingInProgressViewModel.state.collectAsState()

    val agendas = state.agendas
    val segments = state.segments
    val isTopSheetExpanded by agendaViewModel.isTopSheetExpanded
    val firstUncheckedIndex = agendas.indexOfFirst { !it.isCompleted }
    val peekIndex = if (firstUncheckedIndex == -1) agendas.lastIndex else firstUncheckedIndex
    val listState = rememberLazyListState()
    val topSheetHeightPx = remember { mutableStateOf(0) }

    Box(modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (agendas.isNotEmpty() && peekIndex in agendas.indices) {
                TopSheet(
                    expanded = isTopSheetExpanded,
                    onExpandedChange = { agendaViewModel.setTopSheetExpanded(it) },
                    peekContent = {
                        CheckItem(
                            text = agendas[peekIndex].content,
                            checked = agendas[peekIndex].isCompleted,
                            isFocused = !agendas[peekIndex].isCompleted,
                            onToggle = { meetingInProgressViewModel.changeAgendaStatus(agendas[peekIndex].agendaId, !agendas[peekIndex].isCompleted) }
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
                                    onToggle = { meetingInProgressViewModel.changeAgendaStatus(item.agendaId, !item.isCompleted) }
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coordinates ->
                            topSheetHeightPx.value = coordinates.size.height
                        }
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
                        ChatBubble(segment = message)
                        if (index == segments.lastIndex) {
                            Spacer(modifier = Modifier.padding(bottom = 28.dp))
                        }
                    }
                }
            }

        }
//        when {
//            latestFeedback != null && feedbackVisible -> {
//                val elapsed = DateTimeUtils.getElapsedString(startTime, latestFeedback.timestamp)
//
//                Box(
//                    modifier = Modifier
//                        .align(Alignment.TopCenter)
//                        .padding(WindowInsets.statusBars.asPaddingValues())
//                        .padding(top = 48.dp)
//                ) {
//                    SwipeToDismissNotification(
//                        message = latestFeedback.comment,
//                        time = latestFeedback.timestamp,
//                        onDismiss = { feedbackVisible = false }
//                    )
//                }
//            }
//        }
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = with(LocalDensity.current) { topSheetHeightPx.value.toDp() }, start = 20.dp, end = 20.dp)
        ) {
            SwipeToDismissNotification(
                feedback = FeedbackUi("회의 종료까지 10분 남았습니다.\n예정 종료 시각: {종료 예정 시각}", "03:45:12", true),
                onDismiss = {  }
            )
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
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
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
