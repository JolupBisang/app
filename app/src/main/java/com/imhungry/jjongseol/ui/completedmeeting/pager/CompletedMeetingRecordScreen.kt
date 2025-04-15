import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imhungry.jjongseol.R
import com.imhungry.jjongseol.data.chat.ChatMessage
import com.imhungry.jjongseol.ui.component.ChatBubble
import com.imhungry.jjongseol.ui.component.CheckItem
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CompletedMeetingRecordScreen() {
    val chatMessages = remember { dummyMessages() }
    val items = remember { dummyAgenda() }
    val checkedStates = remember { mutableStateListOf(false, false, false, false, false) }
    val lastCheckedIndex = remember { mutableStateOf(0) }
    val firstUncheckedIndex = checkedStates.indexOfFirst { !it }

    val listState = rememberLazyListState()
    var isCollapsed by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemScrollOffset }
            .collectLatest { offset ->
                isCollapsed = when {
                    offset > 100 -> true
                    offset == 0 -> false
                    else -> isCollapsed
                }
            }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

        HeaderSection(
            isCollapsed = isCollapsed,
            isExpanded = isExpanded,
            onToggleCollapse = { isCollapsed = !isCollapsed },
            onToggleAgenda = { isExpanded = !isExpanded },
            items = items,
            checkedStates = checkedStates,
            firstUncheckedIndex = firstUncheckedIndex,
            lastCheckedIndex = lastCheckedIndex
        )

        Divider()

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 8.dp)
        ) {
            items(chatMessages.reversed()) { message ->
                ChatBubble(chatMessage = message)
            }
        }
    }
}

@Composable
fun HeaderSection(
    isCollapsed: Boolean,
    isExpanded: Boolean,
    onToggleCollapse: () -> Unit,
    onToggleAgenda: () -> Unit,
    items: List<String>,
    checkedStates: MutableList<Boolean>,
    firstUncheckedIndex: Int,
    lastCheckedIndex: MutableState<Int>
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "2025.03.26 수, IT관 777호",
                color = Color.Gray,
                fontSize = 13.sp
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onToggleCollapse() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "회의 제목",
                    color = Color.Black,
                    fontSize = 20.sp,
                    modifier = Modifier.weight(1f)
                )

                Image(
                    painter = painterResource(id = R.drawable.edit),
                    contentDescription = "수정 아이콘",
                    modifier = Modifier.size(26.dp)
                )
            }

            AnimatedVisibility(visible = !isCollapsed) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "요약",
                        color = Color(0xFFC5C5C5),
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .border(1.dp, Color(0xFFD3D3D3), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "이 회의는 채식 메뉴와 고기 메뉴 사이의 갈등을 조율하며, 샤브샤브라는 절충안을 도출한 사례입니다.",
                            color = Color.DarkGray
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onToggleAgenda() },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.toggle_arrow),
                            contentDescription = "토글 화살표",
                            modifier = Modifier
                                .size(18.dp)
                                .rotate(if (isExpanded) 90f else 0f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "아젠다",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (isExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp)
                        ) {
                            items.forEachIndexed { i, item ->
                                CheckItem(
                                    text = item,
                                    checked = checkedStates[i],
                                    isFocused = !checkedStates[i] && firstUncheckedIndex == i,
                                    onToggle = {
                                        checkedStates[i] = !checkedStates[i]
                                        if (checkedStates[i]) lastCheckedIndex.value = i
                                    },
                                    topPadding = 14.dp
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("회의 분석", "전체 기록", "피드백 기록").forEach {
                            Text(
                                text = it,
                                color = Color(0xFF1A81D0),
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Divider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color(0xFFDCDCDC))
    )
}

fun dummyMessages(): List<ChatMessage> =
    listOf(
        ChatMessage(
            "원영",
            "이 정도면 꽤 빠른 편임. 자, 얼른 가자!",
            "12:00:45",
            false,
            "https://ca.slack-edge.com/T08CJ94LGP7-U08F17SHYRJ-d89cc306f493-512"
        ),
        ChatMessage(
            "지안",
            "좋아, 그럼 샤브샤브로 가자. 메뉴 정하는 데 10분이나 걸렸네",
            "12:00:23",
            false,
            "https://ca.slack-edge.com/T08CJ94LGP7-U08C6HXPGLX-0271776f36ab-512"
        ),
        ChatMessage("은경", "맵게도 가능하다면 나야 완전 콜이지.", "11:58:38", true),
        ChatMessage(
            "원영",
            "오케이, 나도 찬성. 고기 있고 야채 있고 맵게도 할 수 있으면 다 만족하겠네?",
            "11:57:20",
            false,
            "https://ca.slack-edge.com/T08CJ94LGP7-U08F17SHYRJ-d89cc306f493-512"
        ),
        ChatMessage(
            "유진",
            "나 샤브샤브 좋아! 야채 많이 먹을 수 있어서 딱이야.",
            "11:56:12",
            false,
            "https://ca.slack-edge.com/T08CJ94LGP7-U08F92B4ZF0-bbe9c6ff71e7-512"
        ),
        ChatMessage(
            "상정",
            "샤브샤브? 흐음... 고기가 있긴 하니까 나쁘지 않은데?",
            "11:55:20",
            false,
            "https://ca.slack-edge.com/T08CJ94LGP7-U08FG7QE28K-78dc994d6d30-512"
        ),
        ChatMessage(
            "지안",
            "야… 불닭은 좀... 속 쓰려. 우리 그러면 고기랑 샐러드 둘 다 있는 샤브샤브 어때? 거기 육수도 맵게 할 수 있잖아.",
            "11:54:58",
            false,
            "https://ca.slack-edge.com/T08CJ94LGP7-U08C6HXPGLX-0271776f36ab-512"
        ),
        ChatMessage("은경", "고기도 좋긴 한데, 나 오늘은 불닭 먹고 싶다. 매운 거 완전 땡겨!", "11:54:40", true),
        ChatMessage(
            "유진",
            "또 고기야...? 나 요즘 채식 중이라 좀 곤란한데. 샐러드바 있는 데 어때?",
            "11:54:11",
            false,
            "https://ca.slack-edge.com/T08CJ94LGP7-U08F92B4ZF0-bbe9c6ff71e7-512"
        ),
        ChatMessage(
            "상정",
            "난 삼겹살 땡기는데? 어제부터 고기 생각밖에 안 남.",
            "11:52:23",
            false,
            "https://ca.slack-edge.com/T08CJ94LGP7-U08FG7QE28K-78dc994d6d30-512"
        ),
        ChatMessage(
            "원영",
            "오 또 시작이네, 매일 이러다 결국 돈가스 먹잖아ㅋㅋ 그냥 아무거나 빨리 정하자, 배고파 죽겠어.",
            "11:51:35",
            false,
            "https://ca.slack-edge.com/T08CJ94LGP7-U08F17SHYRJ-d89cc306f493-512"
        ),
        ChatMessage(
            "지안",
            "오늘 점심 뭐 먹을까? 요즘 너무 기름진 거만 먹은 것 같아서 좀 가볍게 가고 싶은데.",
            "11:51:00",
            false,
            "https://ca.slack-edge.com/T08CJ94LGP7-U08C6HXPGLX-0271776f36ab-512"
        ),
    )

fun dummyAgenda(): List<String> = listOf(
    "저메추", "지구는 평평한가?", "35세는 어린이인가?", "가르마 왼쪽 vs 오른쪽", "왼손잡이는 똑똑할까?"
)
