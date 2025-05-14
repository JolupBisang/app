package com.imhungry.jjongseol.ui.meeting.pager

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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
    meetingViewModel: MeetingViewModel = hiltViewModel(),
    agendaViewModel: AgendaViewModel = hiltViewModel(),
    meetingId: Long
) {
    val agendas by agendaViewModel.agendaItems.collectAsState()
    val checkedStates by agendaViewModel.checkedStates.collectAsState()
    val feedbackList by meetingViewModel.sseSubscriber.feedbackList.collectAsState()

    LaunchedEffect(meetingId) {
        agendaViewModel.loadAgendas(meetingId)
    }

    val peekIndex = checkedStates.indexOfFirst { !it }.let { if (it == -1) agendas.lastIndex else it }
    val isLoading = agendas.isEmpty()
    val showTerminationNotification = remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF86CC3B))
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 12.dp)
            ) {
                val chatMessages = remember {
                    listOf(
                        ChatMessage("지안", "오늘 점심 뭐 먹을까? 요즘 너무 기름진 거만 먹은 것 같아서 좀 가볍게 가고 싶은데.", "11:51:00", false, "https://ca.slack-edge.com/T08CJ94LGP7-U08C6HXPGLX-0271776f36ab-512"),
                        ChatMessage("원영", "오 또 시작이네, 매일 이러다 결국 돈가스 먹잖아ㅋㅋ 그냥 아무거나 빨리 정하자, 배고파 죽겠어.", "11:51:35", false, "https://ca.slack-edge.com/T08CJ94LGP7-U08F17SHYRJ-d89cc306f493-512"),
                        ChatMessage("상정", "난 삼겹살 땡기는데? 어제부터 고기 생각밖에 안 남.", "11:52:23", false, "https://ca.slack-edge.com/T08CJ94LGP7-U08FG7QE28K-78dc994d6d30-512"),
                        ChatMessage("유진", "또 고기야...? 나 요즘 채식 중이라 좀 곤란한데. 샐러드바 있는 데 어때?", "11:54:11", false, "https://ca.slack-edge.com/T08CJ94LGP7-U08F92B4ZF0-bbe9c6ff71e7-512"),
                        ChatMessage("은경", "고기도 좋긴 한데, 나 오늘은 불닭 먹고 싶다. 매운 거 완전 땡겨!", "11:54:40", true),
                        ChatMessage("지안", "야… 불닭은 좀... 속 쓰려. 우리 그러면 고기랑 샐러드 둘 다 있는 샤브샤브 어때? 거기 육수도 맵게 할 수 있잖아.", "11:54:58", false, "https://ca.slack-edge.com/T08CJ94LGP7-U08C6HXPGLX-0271776f36ab-512"),
                        ChatMessage("상정", "샤브샤브? 흐음... 고기가 있긴 하니까 나쁘지 않은데?", "11:55:20", false, "https://ca.slack-edge.com/T08CJ94LGP7-U08FG7QE28K-78dc994d6d30-512"),
                        ChatMessage("유진", "나 샤브샤브 좋아! 야채 많이 먹을 수 있어서 딱이야.", "11:56:12", false, "https://ca.slack-edge.com/T08CJ94LGP7-U08F92B4ZF0-bbe9c6ff71e7-512"),
                        ChatMessage("원영", "오케이, 나도 찬성. 고기 있고 야채 있고 맵게도 할 수 있으면 다 만족하겠네?", "11:57:20", false, "https://ca.slack-edge.com/T08CJ94LGP7-U08F17SHYRJ-d89cc306f493-512"),
                        ChatMessage("은경", "맵게도 가능하다면 나야 완전 콜이지.", "11:58:38", true),
                        ChatMessage("지안", "좋아, 그럼 샤브샤브로 가자. 메뉴 정하는 데 10분이나 걸렸네", "12:00:23", false, "https://ca.slack-edge.com/T08CJ94LGP7-U08C6HXPGLX-0271776f36ab-512"),
                        ChatMessage("원영", "이 정도면 꽤 빠른 편임. 자, 얼른 가자!", "12:00:45", false, "https://ca.slack-edge.com/T08CJ94LGP7-U08F17SHYRJ-d89cc306f493-512"),
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 90.dp, bottom = 8.dp),
                    reverseLayout = true
                ) {
                    items(chatMessages.reversed()) { message ->
                        ChatBubble(chatMessage = message)
                    }
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                TopSheet(
                    collapsedHeight = 60.dp,
                    peekContent = {
                        val content = agendas[peekIndex].content
                        val checked = checkedStates[peekIndex]
                        CheckItem(
                            text = content,
                            checked = checked,
                            isFocused = !checked,
                            onToggle = { agendaViewModel.toggleAgendaChecked(peekIndex) }
                        )
                    },
                    content = {
                        Column {
                            agendas.forEachIndexed { i, item ->
                                CheckItem(
                                    text = item.content,
                                    checked = checkedStates[i],
                                    isFocused = !checkedStates[i] && peekIndex == i,
                                    onToggle = { agendaViewModel.toggleAgendaChecked(i) }
                                )
                            }
                        }
                    }
                )

                if (showTerminationNotification.value) {
                    MeetingTerminationNotification(
                        onDismiss = { showTerminationNotification.value = false }
                    )
                }

                val latestFeedback = feedbackList.lastOrNull()
                var visible by remember(latestFeedback) { mutableStateOf(latestFeedback != null) }

                LaunchedEffect(latestFeedback) {
                    if (latestFeedback != null) {
                        visible = true
                        delay(4000)
                        visible = false
                    }
                }

                if (latestFeedback != null && visible) {
                    SwipeToDismissNotification(
                        message = latestFeedback.text,
                        time = latestFeedback.time,
                        onDismiss = { visible = false }
                    )
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
