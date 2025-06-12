import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import androidx.navigation.NavController
import com.imhungry.jjongseol.R
import com.imhungry.jjongseol.data.model.agenda.AgendaItem
import com.imhungry.jjongseol.data.model.meeting.response.MeetingDetailRes
import com.imhungry.jjongseol.data.model.segment.DiarizedSegment
import com.imhungry.jjongseol.data.model.segment.response.SegmentListRes
import com.imhungry.jjongseol.ui.SilRokNavigation
import com.imhungry.jjongseol.ui.completedmeeting.component.MeetingTabRow
import com.imhungry.jjongseol.ui.component.checklist.CheckItem
import com.imhungry.jjongseol.ui.meeting.component.ChatBubble
import com.imhungry.jjongseol.ui.theme.Pretend
import com.imhungry.jjongseol.ui.theme.gray400
import com.imhungry.jjongseol.ui.theme.primaryBackground
import com.imhungry.jjongseol.ui.theme.tertiary
import com.imhungry.jjongseol.util.DateTimeUtils
import com.imhungry.jjongseol.viewmodel.AgendaViewModel

@Composable
fun CompletedMeetingRecordScreen(
    meetingId: Long,
    agendaViewModel: AgendaViewModel = hiltViewModel(),
    navController: NavController,
    segments: List<SegmentListRes>,
    startMillis: Long,
    playbackPosition: Long,
    selectedTab: Int,
    onTabClick: (Int) -> Unit,
    isPlaying: Boolean,
    onSeekToPosition: (Long) -> Unit,
    meetingDetail: MeetingDetailRes,
    currentUserId: Long,
) {
    val context = LocalContext.current
    val lastCheckedIndex = remember { mutableStateOf(0) }
    val agendas by agendaViewModel.agendaItems.collectAsState()
    val listState = rememberLazyListState()
    var isCollapsed by rememberSaveable { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }
    var isScrolling by remember { mutableStateOf(false) }
        // 1. 재생 위치에 해당하는 segment index 찾기
    val currentSegmentIndex = segments.indexOfLast { segment ->
        val elapsed = DateTimeUtils.koreanIsoToMillis(segment.timestamp) - startMillis
        elapsed <= playbackPosition
    }.coerceAtLeast(0)

    // 2. 재생 위치 바뀔 때마다 해당 index로 scroll
    var lastScrolledIndex by rememberSaveable { mutableStateOf(-1) }
    LaunchedEffect(currentSegmentIndex) {
        val first = listState.firstVisibleItemIndex
        val last = (first + listState.layoutInfo.visibleItemsInfo.size - 3).coerceAtLeast(first)
        if (
            segments.isNotEmpty() &&
            currentSegmentIndex != lastScrolledIndex &&
            (currentSegmentIndex < first || currentSegmentIndex > last)
        ) {
            listState.animateScrollToItem(currentSegmentIndex)
            lastScrolledIndex = currentSegmentIndex
        }
    }

    LaunchedEffect(meetingId) {
        agendaViewModel.loadAgendas(meetingId)
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .background(primaryBackground)) {
        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

        HeaderSection(
            isCollapsed = isCollapsed,
            isExpanded = isExpanded,
            onToggleCollapse = { isCollapsed = !isCollapsed },
            onToggleAgenda = { isExpanded = !isExpanded },
            title = meetingDetail.title,
            location = meetingDetail.location,
            scheduledStartTime = meetingDetail.scheduledStartTime,
            agendaItems = agendas,
            lastCheckedIndex = lastCheckedIndex,
            navController = navController,
            selectedTab = selectedTab,
            onTabClick = onTabClick
        )

        Divider()

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
        ) {
            itemsIndexed(segments) { index, segment ->
                if (index == 0) {
                    Spacer(modifier = Modifier.padding(top = 4.dp))
                }

                val prevId = if (index > 0) segments[index - 1].userId else -1L
                val nextId = if (index < segments.lastIndex) segments[index + 1].userId else -1L
                ChatBubble(
                    diarizedSegment = DiarizedSegment(
                        timestamp = segment.timestamp,
                        userId = segment.userId,
                        text = segment.text,
                        order = segment.segmentOrder
                    ),
                    nickname = segment.userName,
                    isMe = (segment.userId == currentUserId),
                    time = DateTimeUtils.getElapsedString(startMillis, segment.timestamp),
                    prevId = prevId,
                    nextId = nextId,
                    highlighted = (index == currentSegmentIndex) && isPlaying,
                    onSegmentClick = { clickedTimestamp ->
                        val seekMillis = DateTimeUtils.isoToMillis(clickedTimestamp) - startMillis
                        onSeekToPosition(seekMillis.coerceAtLeast(0L))
                    }
                )
                if (index == segments.lastIndex) {
                    Spacer(modifier = Modifier.padding(bottom = 30.dp))
                }
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
    title: String,
    location: String,
    scheduledStartTime: String,
    agendaItems: List<AgendaItem>,
    lastCheckedIndex: MutableState<Int>,
    navController: NavController,
    selectedTab: Int,
    onTabClick: (Int) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
//        Column(modifier = Modifier.fillMaxWidth()) {
//            //AnimatedVisibility(visible = !isCollapsed) {
//                Column {
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(top = 12.dp)
//                            .clickable(
//                                interactionSource = remember { MutableInteractionSource() },
//                                indication = null
//                            ) { onToggleCollapse() },
//                        verticalAlignment = Alignment.Top
//                    ) {
//                        Box(
//                            modifier = Modifier.weight(1f)
//                        ) {
//                            Column {
//                                Text(
//                                    text = DateTimeUtils.localIsoToDateString(scheduledStartTime) + ", $location",
//                                    fontFamily = Pretend,
//                                    fontWeight = FontWeight.Medium,
//                                    color = tertiary,
//                                    fontSize = 13.sp
//                                )
//                                Spacer(Modifier.height(8.dp))
//                                Text(
//                                    text = title,
//                                    color = Color.Black,
//                                    fontFamily = Pretend,
//                                    fontWeight = FontWeight.Bold,
//                                    fontSize = 21.sp,
//                                )
//                            }
//                        }
//                        Box(
//                            modifier = Modifier
//                                .wrapContentWidth()
//                                .height(32.dp),
//                            contentAlignment = Alignment.TopEnd
//                        ) {
//                            Image(
//                                painter = painterResource(id = R.drawable.home),
//                                contentDescription = "홈으로",
//                                modifier = Modifier
//                                    .size(26.dp)
//                                    .clickable(
//                                        interactionSource = remember { MutableInteractionSource() },
//                                        indication = null
//                                    ) {
//                                        navController.navigate(SilRokNavigation.Home.route) {
//                                            popUpTo(0)
//                                        }
//                                    }
//                            )
//                        }
//                    }
//                    Column(modifier = Modifier.fillMaxWidth()) {
//                        Row(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(top = 12.dp)
//                                .clickable(
//                                    interactionSource = remember { MutableInteractionSource() },
//                                    indication = null
//                                ) { onToggleAgenda() },
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            Image(
//                                painter = painterResource(id = R.drawable.expand2),
//                                contentDescription = "토글 화살표",
//                                modifier = Modifier
//                                    .size(24.dp)
//                                    .rotate(if (isExpanded) 90f else 0f)
//                            )
//                            Spacer(modifier = Modifier.width(8.dp))
//                            Text(
//                                text = "아젠다",
//                                fontFamily = Pretend,
//                                fontSize = 16.sp,
//                                fontWeight = FontWeight.Bold
//                            )
//                        }
//                        if (isExpanded) {
//                            Column(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .padding(horizontal = 32.dp)
//                            ) {
//                                agendaItems.forEachIndexed { i, item ->
//                                    CheckItem(
//                                        text = item.text,
//                                        checked = item.isCompleted,
//                                        isFocused = false,
//                                        onToggle = {
//                                            lastCheckedIndex.value = i
//                                        },
//                                        topPadding = 8.dp
//                                    )
//                                }
//                            }
//                        }
//                    }
//                }
//            //}
//
//            MeetingTabRow(selectedTab = selectedTab, onTabClick = onTabClick)
//        }
        Column(modifier = Modifier.fillMaxWidth()) {
            if (!isCollapsed) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onToggleCollapse() }, // 클릭 시 접기
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            Column {
                                Text(
                                    text = DateTimeUtils.localIsoToDateString(scheduledStartTime) + ", $location",
                                    fontFamily = Pretend,
                                    fontWeight = FontWeight.Medium,
                                    color = tertiary,
                                    fontSize = 13.sp
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = title,
                                    color = Color.Black,
                                    fontFamily = Pretend,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 21.sp,
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .wrapContentWidth()
                                .height(32.dp),
                            contentAlignment = Alignment.TopEnd
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.home),
                                contentDescription = "홈으로",
                                modifier = Modifier
                                    .size(26.dp)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        navController.navigate(SilRokNavigation.Home.route) {
                                            popUpTo(0)
                                        }
                                    }
                            )
                        }
                    }

                    // 아젠다
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { onToggleAgenda() },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.expand2),
                                contentDescription = "토글 화살표",
                                modifier = Modifier
                                    .size(24.dp)
                                    .rotate(if (isExpanded) 90f else 0f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "아젠다",
                                fontFamily = Pretend,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (isExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 32.dp)
                            ) {
                                agendaItems.forEachIndexed { i, item ->
                                    CheckItem(
                                        text = item.text,
                                        checked = item.isCompleted,
                                        isFocused = false,
                                        onToggle = {
                                            lastCheckedIndex.value = i
                                        },
                                        topPadding = 8.dp
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // 접혀 있을 때도 클릭할 수 있도록 Row만 보여주기
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onToggleCollapse() }
                        .padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontFamily = Pretend,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                }
            }

            MeetingTabRow(selectedTab = selectedTab, onTabClick = onTabClick)
        }

    }
}

@Composable
fun Divider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(1.dp)
            .background(gray400)
    )
}
