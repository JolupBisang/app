package com.imhungry.sillok.presentation.screen.meetingminutes.pager

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imhungry.sillok.R
import com.imhungry.sillok.domain.model.agenda.Agenda
import com.imhungry.sillok.presentation.screen.meeting.component.ChatBubble
import com.imhungry.sillok.presentation.screen.meeting.component.CheckItem
import com.imhungry.sillok.presentation.screen.meetingminutes.components.MeetingTabRow
import com.imhungry.sillok.presentation.util.DateTimeUtils
import com.imhungry.sillok.presentation.viewmodel.meetingminutes.MeetingMinutesViewModel
import com.imhungry.sillok.ui.components.Divider
import com.imhungry.sillok.ui.theme.tertiary

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MeetingMinutesRecordScreen(
    meetingId: Long,
    onBackClick: () -> Unit,
    playbackPosition: Long,
    selectedTab: Int,
    onTabClick: (Int) -> Unit,
    isPlaying: Boolean,
    onSeekToPosition: (Long) -> Unit,
    meetingMinutesViewModel: MeetingMinutesViewModel
) {
    val state by meetingMinutesViewModel.state.collectAsState()

    val agendas = state.agendas
    val segments = state.segments

    val context = LocalContext.current
    val lastCheckedIndex = remember { mutableStateOf(0) }
    val listState = rememberLazyListState()
    var isCollapsed by rememberSaveable { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }
    var isScrolling by remember { mutableStateOf(false) }
    // 1. 재생 위치에 가장 가까운 segment index 찾기
    val currentSegmentIndex = remember(playbackPosition, segments) {
        if (segments.isEmpty()) 0
        else {
            segments.mapIndexedNotNull { index, segment ->
                val elapsed = DateTimeUtils.timeStringToMillis(segment.timestamp)
                if (elapsed != null) {
                    index to kotlin.math.abs(elapsed - playbackPosition)
                } else null
            }.minByOrNull { it.second }?.first ?: 0
        }
    }

    // 2. 재생 위치 바뀔 때마다 해당 index로 scroll
    var lastScrolledIndex by rememberSaveable { mutableStateOf(-1) }
    LaunchedEffect(currentSegmentIndex) {
        if (segments.isEmpty()) return@LaunchedEffect
        
        val first = listState.firstVisibleItemIndex
        val visibleItemCount = listState.layoutInfo.visibleItemsInfo.size
        val last = (first + visibleItemCount - 1).coerceAtLeast(first)
        
        // 화면 하단에서 2개 아이템 이내에 있으면 스크롤하지 않음 (더 여유있게)
        val scrollThreshold = 2
        val shouldScroll = currentSegmentIndex != lastScrolledIndex && (
            currentSegmentIndex < first || 
            currentSegmentIndex > (last - scrollThreshold)
        )
        
        if (shouldScroll) {
            // 세그먼트를 화면에 보이도록 스크롤
            val targetIndex = currentSegmentIndex.coerceIn(0, segments.lastIndex)
            listState.animateScrollToItem(targetIndex)
            lastScrolledIndex = currentSegmentIndex
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        HeaderSection(
            isCollapsed = isCollapsed,
            isExpanded = isExpanded,
            onToggleCollapse = { isCollapsed = !isCollapsed },
            onToggleAgenda = { isExpanded = !isExpanded },
            title = state.meetingTitle,
            location = state.meetingDateAndLocation,
            scheduledStartTime = state.scheduledStartTime,
            agendaItems = agendas,
            lastCheckedIndex = lastCheckedIndex,
            onBackClick = onBackClick,
            selectedTab = selectedTab,
            onTabClick = onTabClick
        )

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

                ChatBubble(
                    segment = segment,
                    highlighted = (isPlaying || playbackPosition > 0) && index == currentSegmentIndex,
                    onSegmentClick = { clickedTimestamp ->
                        val seekMillis = DateTimeUtils.timeStringToMillis(clickedTimestamp)
                        if (seekMillis != null) {
                            onSeekToPosition(seekMillis.coerceAtLeast(0L))
                        }
                    }
                )
                if (index == segments.lastIndex) {
                    Spacer(modifier = Modifier.padding(bottom = 30.dp))
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HeaderSection(
    isCollapsed: Boolean,
    isExpanded: Boolean,
    onToggleCollapse: () -> Unit,
    onToggleAgenda: () -> Unit,
    title: String,
    location: String,
    scheduledStartTime: String,
    agendaItems: List<Agenda>,
    lastCheckedIndex: MutableState<Int>,
    onBackClick: () -> Unit,
    selectedTab: Int,
    onTabClick: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
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
                                    text = location,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Normal,
                                    color = tertiary,
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleMedium,
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
                                        onBackClick()
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
                                text = "주제",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        AnimatedVisibility(
                            visible = isExpanded,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 32.dp, end = 32.dp, top = 8.dp)
                            ) {
                                agendaItems.forEachIndexed { i, item ->
                                    CheckItem(
                                        text = item.content,
                                        checked = item.isCompleted,
                                        isFocused = false,
                                        onToggle = {
                                            lastCheckedIndex.value = i
                                        },
                                        bottomPadding = 8.dp
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
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }

            MeetingTabRow(selectedTab = selectedTab, onTabClick = onTabClick)
        }

    }
}
