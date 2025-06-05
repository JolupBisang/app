package com.imhungry.jjongseol.ui.meeting.pager

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.imhungry.jjongseol.R
import com.imhungry.jjongseol.ui.SilRokNavigation
import com.imhungry.jjongseol.ui.meeting.component.ChatBubble
import com.imhungry.jjongseol.ui.component.checklist.CheckItem
import com.imhungry.jjongseol.ui.component.dialog.MeetingTerminationNotification
import com.imhungry.jjongseol.ui.component.feedback.Notification
import com.imhungry.jjongseol.ui.login.LoginScreen
import com.imhungry.jjongseol.ui.meeting.component.TopSheet
import com.imhungry.jjongseol.ui.theme.primaryBackground
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
    navController: NavController
) {
    val meetingDetail by meetingViewModel.meetingDetail.collectAsState()
    val agendas by agendaViewModel.agendaItems.collectAsState()
    val isTopSheetExpanded by meetingViewModel.isTopSheetExpanded
    val firstUncheckedIndex = agendas.indexOfFirst { !it.isCompleted }
    val peekIndex = if (firstUncheckedIndex == -1) agendas.lastIndex else firstUncheckedIndex
    val hasAgendas = agendas.isNotEmpty()
    val showTerminationNotification = remember { mutableStateOf(false) }
    val feedbackList by meetingViewModel.feedbackList.collectAsState()
    val diarizedSegments by meetingViewModel.diarizedSegments.collectAsState()
    val latestFeedback = feedbackList.lastOrNull()
    var feedbackVisible by remember(latestFeedback) { mutableStateOf(latestFeedback != null) }
    val listState = rememberLazyListState()

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

    Box(modifier = Modifier
        .fillMaxSize()
        .background(primaryBackground)
        .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp, bottom = 8.dp),
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
                            text = agendas[peekIndex].content,
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
                                    text = item.content,
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
                        if (index == 0) {
                            Spacer(modifier = Modifier.padding(top = 4.dp))
                        }
                        ChatBubble(
                            diarizedSegment = message,
                            isMe = message.order % 2 == 0,
                            index = message.order
                        )
                        if (index == diarizedSegments.lastIndex) {
                            Spacer(modifier = Modifier.padding(bottom = 28.dp))
                        }
                    }
                }
            }
        }
        when {
            showTerminationNotification.value -> {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp)
                ) {
                    MeetingTerminationNotification(
                        onDismiss = { showTerminationNotification.value = false }
                    )
                }
            }
            latestFeedback != null && feedbackVisible -> {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(WindowInsets.statusBars.asPaddingValues())
                        .padding(top = 48.dp)
                ) {
                    SwipeToDismissNotification(
                        message = latestFeedback.comment,
                        time = latestFeedback.timestamp,
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