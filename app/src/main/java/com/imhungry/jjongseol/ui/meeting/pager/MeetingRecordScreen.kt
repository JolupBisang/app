package com.imhungry.jjongseol.ui.meeting.pager

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.imhungry.jjongseol.data.chat.ChatMessage
import com.imhungry.jjongseol.ui.component.ChatBubble
import com.imhungry.jjongseol.ui.component.Notification
import com.imhungry.jjongseol.ui.component.TopSheet

@Composable
fun MeetingRecordScreen() {
    val items = listOf(
        "저메추", "지구는 평평한가?", "35세는 어린이인가?", "가르마 왼쪽 vs 오른쪽", "왼손잡이는 똑똑할까?"
    )
    val checkedStates = remember { mutableStateListOf(false, false, false, false, false) }
    val lastCheckedIndex = remember { mutableStateOf(0) }

    val firstUncheckedIndex = checkedStates.indexOfFirst { !it }
    val peekIndex = if (firstUncheckedIndex == -1) lastCheckedIndex.value else firstUncheckedIndex

    val showNotification = remember { mutableStateOf(false) }
    val message = remember { mutableStateOf("우리, 이쁜.딸. 얼굴만큼.고운.말 스자.^^ -엄마가") }
    val timestamp = remember { mutableStateOf("00:02:10") }

    Box(modifier = Modifier.fillMaxSize()) {
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

        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            TopSheet(
                collapsedHeight = 60.dp,
                peekContent = {
                    CheckItem(
                        text = items[peekIndex],
                        checked = checkedStates[peekIndex],
                        isFocused = !checkedStates[peekIndex],
                        onToggle = {
                            checkedStates[peekIndex] = !checkedStates[peekIndex]
                            if (checkedStates[peekIndex]) lastCheckedIndex.value = peekIndex
                        }
                    )
                },
                content = {
                    Column {
                        items.forEachIndexed { i, item ->
                            CheckItem(
                                text = item,
                                checked = checkedStates[i],
                                isFocused = !checkedStates[i] && firstUncheckedIndex == i,
                                onToggle = {
                                    checkedStates[i] = !checkedStates[i]
                                    if (checkedStates[i]) lastCheckedIndex.value = i
                                }
                            )
                        }
                    }
                }
            )

            if (showNotification.value) {
                Spacer(modifier = Modifier.height(8.dp))
                Notification(
                    visible = true,
                    message = message.value,
                    time = timestamp.value,
                    isRead = true
                )
            }
        }
    }
}

@Composable
fun CheckItem(
    text: String,
    checked: Boolean,
    isFocused: Boolean,
    onToggle: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {
        Box(
            modifier = Modifier.clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onToggle
            )
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = null,
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFFCBE7FD),
                    uncheckedColor = Color(0xFF2196F3),
                    checkmarkColor = Color.White,
                )
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Normal,
                textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None
            ),
            color = when {
                checked -> Color(0xFFB7B7B7)
                isFocused -> Color.Gray
                else -> Color(0xFFB7B7B7)
            },
            modifier = Modifier.padding(bottom = 2.dp)
        )
    }
}
