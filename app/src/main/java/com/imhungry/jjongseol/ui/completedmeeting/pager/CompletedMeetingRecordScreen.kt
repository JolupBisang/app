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
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.imhungry.jjongseol.R
import com.imhungry.jjongseol.data.model.segment.DiarizedSegment
import com.imhungry.jjongseol.data.model.user.response.UserInfoResponse
import com.imhungry.jjongseol.data.network.config.AppPrefs
import com.imhungry.jjongseol.ui.meeting.component.ChatBubble
import com.imhungry.jjongseol.ui.component.checklist.CheckItem
import com.imhungry.jjongseol.viewmodel.SegmentViewModel
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

@Composable
fun CompletedMeetingRecordScreen(
    meetingId: Long,
    segmentViewModel: SegmentViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val appPrefs = remember { AppPrefs(context) }
    val myProfile: UserInfoResponse? = appPrefs.loadMyProfile()
    val currentUserId: Long? = myProfile?.id

    val items = remember { dummyAgenda() }
    val checkedStates = remember { mutableStateListOf(false, false, false, false, false) }
    val lastCheckedIndex = remember { mutableStateOf(0) }
    val firstUncheckedIndex = checkedStates.indexOfFirst { !it }
    val segments by segmentViewModel.segments.collectAsState()
    val listState = rememberLazyListState()
    var isCollapsed by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(meetingId) {
        segmentViewModel.loadSegments(meetingId, reset = true)
    }

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
                .padding(bottom = 8.dp),
        ) {
            items(segments) { segment ->
                ChatBubble(
                    diarizedSegment = DiarizedSegment(
                        timestamp = segment.timestamp,
                        userId = segment.userId,
                        text = segment.text,
                        order = segment.segmentOrder
                    ),
                    nickname = segment.userName,
                    isMe = (segment.userId == currentUserId),
                    time = formatKoreanTime(segment.timestamp)
                )
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


fun dummyAgenda(): List<String> = listOf(
    "저메추", "지구는 평평한가?", "35세는 어린이인가?", "가르마 왼쪽 vs 오른쪽", "왼손잡이는 똑똑할까?"
)

fun formatKoreanTime(isoString: String): String {
    return try {
        val dateTime = LocalDateTime.parse(isoString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val hour24 = dateTime.hour
        val minute = dateTime.minute

        val amPm = if (hour24 < 12) "오전" else "오후"
        val hour12 = when {
            hour24 == 0 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }
        "$amPm ${hour12}시 ${minute}분"
    } catch (e: Exception) {
        ""
    }
}