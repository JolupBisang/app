package com.imhungry.jjongseol.ui.meeting.pager

import android.content.Intent
import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.imhungry.jjongseol.R
import com.imhungry.jjongseol.data.model.meeting.MeetingStatus
import com.imhungry.jjongseol.data.model.user.response.UserInfoResponse
import com.imhungry.jjongseol.data.network.config.AppPrefs
import com.imhungry.jjongseol.service.MeetingSseService
import com.imhungry.jjongseol.ui.SilRokNavigation
import com.imhungry.jjongseol.ui.meeting.component.ChatBubble
import com.imhungry.jjongseol.ui.component.checklist.CheckItem
import com.imhungry.jjongseol.ui.component.dialog.MeetingTerminationNotification
import com.imhungry.jjongseol.ui.component.feedback.Notification
import com.imhungry.jjongseol.ui.login.LoginScreen
import com.imhungry.jjongseol.ui.meeting.component.BreakFeedbackChecker
import com.imhungry.jjongseol.ui.meeting.component.EndFeedbackChecker
import com.imhungry.jjongseol.ui.meeting.component.TopSheet
import com.imhungry.jjongseol.ui.theme.primaryBackground
import com.imhungry.jjongseol.util.DateTimeUtils
import com.imhungry.jjongseol.viewmodel.AgendaViewModel
import com.imhungry.jjongseol.viewmodel.MeetingViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun MeetingRecordScreen(
    meetingViewModel: MeetingViewModel,
    agendaViewModel: AgendaViewModel,
    meetingId: Long,
    navController: NavController,
    participantInfos: List<UserInfoResponse>,
    startTime: Long?
) {
    val context = LocalContext.current

    val meetingDetail by meetingViewModel.meetingDetail.collectAsState()
    val agendas by agendaViewModel.agendaItems.collectAsState()
    val isTopSheetExpanded by meetingViewModel.isTopSheetExpanded
    val firstUncheckedIndex = agendas.indexOfFirst { !it.isCompleted }
    val peekIndex = if (firstUncheckedIndex == -1) agendas.lastIndex else firstUncheckedIndex
    val hasAgendas = agendas.isNotEmpty()
    val feedbackList by meetingViewModel.feedbackList.collectAsState()
    val diarizedSegments by meetingViewModel.diarizedSegments.collectAsState()
    val latestFeedback = feedbackList.lastOrNull()
    var feedbackVisible by remember(latestFeedback) { mutableStateOf(latestFeedback != null) }
    val listState = rememberLazyListState()
    val nicknameMap = remember(participantInfos) {
        participantInfos.associateBy({ it.id }, { it.nickname })
    }
    val appPrefs = remember { AppPrefs(context) }
    val myProfile: UserInfoResponse? = appPrefs.loadMyProfile()
    val myUserId: Long? = myProfile?.id

    LaunchedEffect(diarizedSegments.size) {
        if (diarizedSegments.isNotEmpty()) {
            listState.animateScrollToItem(diarizedSegments.size - 1)
        }
    }
    LaunchedEffect(latestFeedback) {
        if (latestFeedback != null) {
            feedbackVisible = true
            delay(4000)
            feedbackVisible = false
        }
    }
    BreakFeedbackChecker(
        meetingId = meetingId,
        meetingDetail = meetingDetail,
        startTime = startTime,
        feedbackList = feedbackList
    ) { newFeedback ->
        meetingViewModel.addFeedback(newFeedback)
    }
    EndFeedbackChecker(
        meetingId = meetingId,
        meetingDetail = meetingDetail,
        startTime = startTime,
        feedbackList = feedbackList,
        onAddFeedback = { meetingViewModel.addFeedback(it) }
    )
    Box(modifier = Modifier
        .fillMaxSize()
        .background(primaryBackground)
        .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.back),
                    contentDescription = "뒤로가기",
                    modifier = Modifier
                        .size(24.dp)
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
            if (hasAgendas && peekIndex in agendas.indices) {
                TopSheet(
                    expanded = isTopSheetExpanded,
                    onExpandedChange = { meetingViewModel.setTopSheetExpanded(it) },
                    peekContent = {
                        CheckItem(
                            text = agendas[peekIndex].text,
                            checked = agendas[peekIndex].isCompleted,
                            isFocused = !agendas[peekIndex].isCompleted,
                            onToggle = { agendaViewModel.onToggleAgenda(peekIndex) }
                        )
                    },
                    content = {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 161.dp)
                        ) {
                            itemsIndexed(agendas) { i, item ->
                                CheckItem(
                                    text = item.text,
                                    checked = item.isCompleted,
                                    isFocused = !item.isCompleted && firstUncheckedIndex == i,
                                    onToggle = { agendaViewModel.onToggleAgenda(i) }
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                itemsIndexed(diarizedSegments) { index, message ->
                    if (message.text.isNotBlank()) {
                        val elapsed = DateTimeUtils.getElapsedString(startTime, message.timestamp)

                        if (index == 0) {
                            Spacer(modifier = Modifier.padding(top = 4.dp))
                        }
                        ChatBubble(
                            diarizedSegment = message,
                            nickname = nicknameMap[message.userId] ?: "알 수 없음",
                            isMe = message.userId == myUserId,
                            time = elapsed
                        )
                        if (index == diarizedSegments.lastIndex) {
                            Spacer(modifier = Modifier.padding(bottom = 28.dp))
                        }
                    }
                }
            }
        }
        when {
            latestFeedback != null && feedbackVisible -> {
                val elapsed = DateTimeUtils.getElapsedString(startTime, latestFeedback.timestamp)

                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(WindowInsets.statusBars.asPaddingValues())
                        .padding(top = 48.dp)
                ) {
                    SwipeToDismissNotification(
                        message = latestFeedback.comment,
                        time = elapsed,
                        onDismiss = { feedbackVisible = false }
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