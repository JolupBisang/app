import androidx.compose.animation.AnimatedVisibility
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
import androidx.navigation.NavController
import com.imhungry.jjongseol.R
import com.imhungry.jjongseol.data.model.agenda.AgendaItem
import com.imhungry.jjongseol.data.model.segment.DiarizedSegment
import com.imhungry.jjongseol.data.model.segment.response.SegmentListRes
import com.imhungry.jjongseol.data.model.user.response.UserInfoResponse
import com.imhungry.jjongseol.data.network.config.AppPrefs
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
import com.imhungry.jjongseol.viewmodel.MeetingViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CompletedMeetingRecordScreen(
    meetingId: Long,
    agendaViewModel: AgendaViewModel = hiltViewModel(),
    meetingViewModel: MeetingViewModel = hiltViewModel(),
    navController: NavController,
    segments: List<SegmentListRes>,
    startMillis: Long,
    selectedTab: Int,
    onTabClick: (Int) -> Unit,
) {
    val context = LocalContext.current
    val agendaLoading by agendaViewModel.isLoading.collectAsState()
    val meetingLoading by meetingViewModel.isLoading.collectAsState()
    val isLoading = agendaLoading || meetingLoading
    val appPrefs = remember { AppPrefs(context) }
    val myProfile: UserInfoResponse? = appPrefs.loadMyProfile()
    val currentUserId: Long? = myProfile?.id
    val lastCheckedIndex = remember { mutableStateOf(0) }
    val meetingDetail by meetingViewModel.meetingDetail.collectAsState()
    val agendas by agendaViewModel.agendaItems.collectAsState()
    val title = meetingDetail?.title ?: ""
    val location = meetingDetail?.location ?: ""
    val scheduledStartTime = meetingDetail?.scheduledStartTime ?: ""
    val listState = rememberLazyListState()
    var isCollapsed by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }
    val summary = "회의 요약이 없습니다."


    LaunchedEffect(meetingId) {
        agendaViewModel.loadAgendas(meetingId)
        meetingViewModel.loadMeetingDetail2(meetingId)
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

    if (!isLoading) {
        Column(modifier = Modifier
            .fillMaxSize()
            .background(primaryBackground)) {
            Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

            HeaderSection(
                isCollapsed = isCollapsed,
                isExpanded = isExpanded,
                onToggleCollapse = { isCollapsed = !isCollapsed },
                onToggleAgenda = { isExpanded = !isExpanded },
                title = title,
                location = location,
                scheduledStartTime = scheduledStartTime,
                summary = summary,
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
                    .fillMaxSize()
            ) {
                itemsIndexed(segments) { index, segment ->
                    if (index == 0) {
                        Spacer(modifier = Modifier.padding(top = 4.dp))
                    }
                    ChatBubble(
                        diarizedSegment = DiarizedSegment(
                            timestamp = segment.timestamp,
                            userId = segment.userId,
                            text = segment.text,
                            order = segment.segmentOrder
                        ),
                        nickname = segment.userName,
                        isMe = (segment.userId == currentUserId),
                        time = DateTimeUtils.getElapsedString(startMillis, segment.timestamp)
                    )
                    if (index == segments.lastIndex) {
                        Spacer(modifier = Modifier.padding(bottom = 28.dp))
                    }
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
    summary: String,
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
        Column(modifier = Modifier.fillMaxWidth()) {
            AnimatedVisibility(visible = !isCollapsed) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onToggleCollapse() },
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier.weight(1f)
                        ) {
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
