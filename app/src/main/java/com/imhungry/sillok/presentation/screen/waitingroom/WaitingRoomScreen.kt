package com.imhungry.sillok.presentation.screen.waitingroom

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.imhungry.sillok.R
import com.imhungry.sillok.presentation.screen.meeting.component.CheckItem
import com.imhungry.sillok.presentation.screen.meeting.component.TopSheet
import com.imhungry.sillok.presentation.state.waitingroom.WaitingRoomEvent
import com.imhungry.sillok.presentation.viewmodel.meeting.AgendaViewModel
import com.imhungry.sillok.presentation.viewmodel.waitingroom.WaitingRoomViewModel
import com.imhungry.sillok.ui.components.MeetingBasicBox
import com.imhungry.sillok.ui.components.SillokTextButton
import com.imhungry.sillok.ui.theme.disabled
import com.imhungry.sillok.ui.theme.primaryBackground
import com.imhungry.sillok.ui.theme.primarySurface
import com.imhungry.sillok.ui.theme.shadow
import com.imhungry.sillok.ui.theme.tertiary
import com.imhungry.sillok.ui.theme.whiteBackground

@Composable
fun WaitingRoomScreen(
    meetingId: Long,
    onStartMeeting: () -> Unit,
    waitingRoomViewModel: WaitingRoomViewModel = hiltViewModel(),
    agendaViewModel: AgendaViewModel = hiltViewModel()
) {
    val state by waitingRoomViewModel.state.collectAsState()

    // 진입 시 아젠다/회의 상세 로드
    LaunchedEffect(meetingId) {
        //waitingRoomViewModel.loadAgendasAndMeetingDetail(meetingId)
        waitingRoomViewModel.loadDummyWaitingRoomState()
    }

    // 회의 시작/실패 이벤트 처리
    LaunchedEffect(Unit) {
        waitingRoomViewModel.events.collect { event ->
            when (event) {
                is WaitingRoomEvent.MeetingStarted -> onStartMeeting()
                is WaitingRoomEvent.StartFailed -> { /* no-op, state.error already set */ }
            }
        }
    }

    val agendas = state.agendas
    val isTopSheetExpanded by agendaViewModel.isTopSheetExpanded
    val firstUncheckedIndex = agendas.indexOfFirst { !it.isCompleted }
    val peekIndex = if (firstUncheckedIndex == -1) agendas.lastIndex else firstUncheckedIndex

    MeetingBasicBox(
        navigationBarColor = whiteBackground,
        backgroundColor = primaryBackground
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(bottom = 20.dp)
        ) {
            TopSheet(
                expanded = isTopSheetExpanded,
                onExpandedChange = { agendaViewModel.setTopSheetExpanded(it) },
                peekContent = {
                    if (agendas.isNotEmpty()) {
                        val item = agendas[peekIndex]
                        CheckItem(
                            text = item.content,
                            checked = item.isCompleted,
                            isFocused = !item.isCompleted,
                            onToggle = { waitingRoomViewModel.changeAgendaStatus(meetingId, item.agendaId, !item.isCompleted) }
                        )
                    }
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
                                onToggle = { waitingRoomViewModel.changeAgendaStatus(meetingId, item.agendaId, !item.isCompleted) }
                            )
                        }
                    }
                }
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "회의가 시작되길 기다리는 중",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                        color = tertiary,
                        fontWeight = FontWeight.Medium
                    )
                    SillokTextButton(
                        text = "시작하기",
                        onClick = {
                            //waitingRoomViewModel.startMeeting()
                            onStartMeeting()
                        },
                        modifier = Modifier.padding(top = 28.dp),
                        textColor = primarySurface,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                shadow
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(whiteBackground)
                    .padding(start = 20.dp, end = 20.dp, top = 12.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "00:00:00",
                        style = MaterialTheme.typography.titleMedium,
                        color = disabled,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    Text(
                        text = state.targetTimeDisplay,
                        style = MaterialTheme.typography.bodySmall,
                        color = disabled,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.power),
                        contentDescription = "종료",
                        modifier = Modifier.size(24.dp),
                        colorFilter = ColorFilter.tint(disabled)
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.mic),
                            contentDescription = "마이크",
                            modifier = Modifier.size(28.dp),
                            colorFilter = ColorFilter.tint(disabled)
                        )
                    }

                    Image(
                        painter = painterResource(id = R.drawable.out),
                        contentDescription = "나가기",
                        modifier = Modifier.size(24.dp),
                        colorFilter = ColorFilter.tint(disabled)
                    )
                }
            }
        }
    }
}