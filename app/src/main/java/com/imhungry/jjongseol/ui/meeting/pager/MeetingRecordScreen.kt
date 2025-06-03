package com.imhungry.jjongseol.ui.meeting.pager

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.imhungry.jjongseol.data.model.chat.ChatMessage
import com.imhungry.jjongseol.ui.component.chat.ChatBubble
import com.imhungry.jjongseol.ui.component.checklist.CheckItem
import com.imhungry.jjongseol.ui.component.dialog.MeetingTerminationNotification
import com.imhungry.jjongseol.ui.component.feedback.Notification
import com.imhungry.jjongseol.ui.component.layout.TopSheet
import com.imhungry.jjongseol.viewmodel.AgendaViewModel
import com.imhungry.jjongseol.viewmodel.MeetingViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MeetingRecordScreen(
    meetingViewModel: MeetingViewModel,
    agendaViewModel: AgendaViewModel,
    meetingId: Long
) {
    val meetingDetail by meetingViewModel.meetingDetail.collectAsState()
    val agendas by agendaViewModel.agendaItems.collectAsState()
    var isTopSheetExpanded by remember { mutableStateOf(false) }

    val firstUncheckedIndex = agendas.indexOfFirst { !it.isCompleted }
    val peekIndex = if (firstUncheckedIndex == -1) agendas.lastIndex else firstUncheckedIndex
    val hasAgendas = agendas.isNotEmpty()
    val showTerminationNotification = remember { mutableStateOf(false) }
    val feedbackList by meetingViewModel.feedbackList.collectAsState()

    val chatMessages = remember {
        listOf(
            ChatMessage("지안", "오늘 점심 뭐 먹을까?", "11:51:00", false, "https://ca.slack-edge.com/T08CJ94LGP7-U08C6HXPGLX-0271776f36ab-512"),
            ChatMessage("원영", "아무거나 빨리 정하자, 배고파 죽겠어.", "11:51:35", false, "https://ca.slack-edge.com/T08CJ94LGP7-U08F17SHYRJ-d89cc306f493-512"),
            ChatMessage("상정", "난 삼겹살 땡기는데? 어제부터 고기 생각밖에 안 남.", "11:52:23", false, "https://ca.slack-edge.com/T08CJ94LGP7-U08FG7QE28K-78dc994d6d30-512"),
            ChatMessage("유진", "또 고기야...? 샐러드바 있는 데 어때?", "11:54:11", false, "https://ca.slack-edge.com/T08CJ94LGP7-U08F92B4ZF0-bbe9c6ff71e7-512"),
            ChatMessage("은경", "고기도 좋긴 한데, 나 오늘은 불닭 먹고 싶다. 매운 거 완전 땡겨!", "11:54:40", true),
            ChatMessage("지안", "우리 그러면 고기랑 샐러드 둘 다 있는 샤브샤브 어때? 거기 육수도 맵게 할 수 있잖아.", "11:54:58", false, "https://ca.slack-edge.com/T08CJ94LGP7-U08C6HXPGLX-0271776f36ab-512"),
            ChatMessage("상정", "고기가 있긴 하니까 나쁘지 않은데?", "11:55:20", false, "https://ca.slack-edge.com/T08CJ94LGP7-U08FG7QE28K-78dc994d6d30-512"),
            ChatMessage("유진", "샤브샤브 좋아!", "11:56:12", false, "https://ca.slack-edge.com/T08CJ94LGP7-U08F92B4ZF0-bbe9c6ff71e7-512"),
            ChatMessage("원영", "오케이, 나도 찬성. 고기 있고 야채 있고 맵게도 할 수 있으면 다 만족하겠네?", "11:57:20", false, "https://ca.slack-edge.com/T08CJ94LGP7-U08F17SHYRJ-d89cc306f493-512"),
            ChatMessage("은경", "맵게도 가능하다면 나야 완전 콜이지.", "11:58:38", true),
            ChatMessage("지안", "좋아, 그럼 샤브샤브로 가자. 메뉴 정하는 데 10분이나 걸렸네", "12:00:23", false, "https://ca.slack-edge.com/T08CJ94LGP7-U08C6HXPGLX-0271776f36ab-512"),
            ChatMessage("원영", "이 정도면 꽤 빠른 편임. 자, 얼른 가자!", "12:00:45", false, "https://ca.slack-edge.com/T08CJ94LGP7-U08F17SHYRJ-d89cc306f493-512"),
        )
    }

    val latestFeedback = feedbackList.lastOrNull()
    var feedbackVisible by remember(latestFeedback) { mutableStateOf(latestFeedback != null) }
    LaunchedEffect(latestFeedback) {
        if (latestFeedback != null) {
            feedbackVisible = true
            delay(4000)
            feedbackVisible = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        if (hasAgendas && peekIndex in agendas.indices) {
            TopSheet(
                modifier = Modifier.background(Color(0xFFE5E5E5)),
                expanded = isTopSheetExpanded,
                onExpandedChange = { isTopSheetExpanded = it },
                peekContent = {
                    CheckItem(
                        text = agendas[peekIndex].content,
                        checked = agendas[peekIndex].isCompleted,
                        isFocused = !agendas[peekIndex].isCompleted,
                        onToggle = { agendaViewModel.onToggleAgenda(peekIndex) }
                    )
                },
                content = {
                    LazyColumn (
                        modifier = Modifier.heightIn(max = 161.dp)
                    ) {
                        itemsIndexed(agendas) { i, item ->
                            CheckItem(
                                text = item.content,
                                checked = item.isCompleted,
                                isFocused = !item.isCompleted && firstUncheckedIndex == i,
                                onToggle = { agendaViewModel.onToggleAgenda(i) }
                            )
                        }
                    }
                }
            )
        }

        when {
            showTerminationNotification.value -> {
                MeetingTerminationNotification(
                    onDismiss = { showTerminationNotification.value = false }
                )
            }
            latestFeedback != null && feedbackVisible -> {
                SwipeToDismissNotification(
                    message = latestFeedback.comment,
                    time = latestFeedback.timestamp,
                    onDismiss = { feedbackVisible = false }
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            reverseLayout = true
        ) {
            itemsIndexed(chatMessages.reversed()) { index, message ->
                if (index == 0) {
                    Spacer(modifier = Modifier.padding(top = 8.dp))
                }
                ChatBubble(chatMessage = message)
                if (index == chatMessages.lastIndex) {
                    Spacer(modifier = Modifier.padding(bottom = 48.dp))
                }
            }
        }
    }
}

@Composable
fun SwipeToDismissNotification(
    message: String,
    time: String,
    onDismiss: () -> Unit
) {
    val offsetX = remember { Animatable(0f) }
    val threshold = 200f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .pointerInput(Unit) {
                coroutineScope {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { _, dragAmount ->
                            launch {
                                val newOffset = offsetX.value + dragAmount
                                offsetX.snapTo(newOffset.coerceIn(-1000f, 1000f))
                            }
                        }
                    )
                }
            }
            .pointerInput(Unit) {
                coroutineScope {
                    detectDragEnd {
                        if (kotlin.math.abs(offsetX.value) > threshold) {
                            onDismiss()
                        } else {
                            launch {
                                offsetX.animateTo(0f)
                            }
                        }
                    }
                }
            }
            .offset { IntOffset(offsetX.value.toInt(), 0) }
    ) {
        Notification(
            visible = true,
            message = message,
            time = time,
            isRead = true
        )
    }
}

suspend fun PointerInputScope.detectDragEnd(onDragEnd: () -> Unit) {
    coroutineScope {
        awaitPointerEventScope {
            do {
                val event = awaitPointerEvent()
                val change = event.changes.firstOrNull()
                if (change?.changedToUpIgnoreConsumed() == true) {
                    onDragEnd()
                    break
                }
            } while (true)
        }
    }
}
