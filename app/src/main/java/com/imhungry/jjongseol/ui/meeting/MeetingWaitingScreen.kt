package com.imhungry.jjongseol.ui.meeting

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.imhungry.jjongseol.R
import com.imhungry.jjongseol.data.model.meeting.MeetingStatus
import com.imhungry.jjongseol.ui.SilRokNavigation
import com.imhungry.jjongseol.ui.component.checklist.CheckItem
import com.imhungry.jjongseol.ui.component.dialog.ErrorDialogHandler
import com.imhungry.jjongseol.ui.component.layout.TopSheet
import com.imhungry.jjongseol.ui.meeting.bottom.MeetingControlPanel
import com.imhungry.jjongseol.ui.theme.Pretend
import com.imhungry.jjongseol.ui.theme.SetNavigationBarColor
import com.imhungry.jjongseol.viewmodel.AgendaViewModel
import com.imhungry.jjongseol.viewmodel.LoginViewModel
import com.imhungry.jjongseol.viewmodel.MeetingViewModel

@Composable
fun MeetingWaitingScreen(
    loginViewModel: LoginViewModel,
    meetingViewModel: MeetingViewModel,
    agendaViewModel: AgendaViewModel,
    onFinish: (SilRokNavigation) -> Unit,
    meetingId: Long = 1L
) {
    val meetingDetail by meetingViewModel.meetingDetail.collectAsState()
    val agendas by agendaViewModel.agendaItems.collectAsState()
    val agendaError by agendaViewModel.errorMessage.collectAsState()
    val meetingError by meetingViewModel.errorMessage.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf<String?>(null) }
    val meetingStatus by meetingViewModel.meetingStatus.collectAsState()
    val isMeetingLoading by meetingViewModel.isLoading.collectAsState()
    val isAgendaLoading by agendaViewModel.isLoading.collectAsState()
    val isStatusUpdating by meetingViewModel.isStatusUpdating.collectAsState()
    var isTopSheetExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(meetingId) {
        meetingViewModel.loadMeetingDetail2(meetingId)
    }

    LaunchedEffect(meetingDetail) {
        if (meetingDetail != null) {
            agendaViewModel.loadAgendas(meetingId)
        }
    }

    val remainingTime by remember(meetingDetail) {
        mutableStateOf(formatTargetTime(meetingDetail?.targetTime))
    }

    LaunchedEffect(meetingError, agendaError) {
        dialogMessage = meetingError ?: agendaError
        showDialog = dialogMessage != null
    }

    LaunchedEffect(meetingStatus) {
        if (meetingStatus == MeetingStatus.IN_PROGRESS) {
            onFinish(SilRokNavigation.Meeting)
        }
    }

    val firstUncheckedIndex = agendas.indexOfFirst { !it.isCompleted }
    val peekIndex = if (firstUncheckedIndex == -1) agendas.lastIndex else firstUncheckedIndex
    val showLoading = isMeetingLoading || isAgendaLoading || isStatusUpdating

    SetNavigationBarColor(Color(0xFFE5E5E5))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE5E5E5))
    ) {
        ErrorDialogHandler(
            errorMessage = dialogMessage,
            showDialog = showDialog,
            onFinish = onFinish,
            clearError = { meetingViewModel.clearErrorMessage() },
            loginViewModel = loginViewModel
        )

        if (!isAgendaLoading && peekIndex in agendas.indices) {
            TopSheet(
                expanded = isTopSheetExpanded,
                onExpandedChange = { isTopSheetExpanded = it },
                peekContent = {
                    CheckItem(
                        text = agendas[peekIndex].content,
                        checked = agendas[peekIndex].isCompleted,
                        isFocused = !agendas[peekIndex].isCompleted,
                        onToggle = { agendaViewModel.onToggleAgenda(peekIndex) }
                    )
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
                                onToggle = { agendaViewModel.onToggleAgenda(i) }
                            )
                        }
                    }
                }
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            if (showLoading) {
                CircularProgressIndicator(color = Color(0xFF969696))
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.waiting_guidance),
                        textAlign = TextAlign.Center,
                        color = Color(0xFF999999),
                        fontFamily = Pretend,
                        fontWeight = FontWeight.Bold
                    )
                    StartButton(onClick = {
                        meetingViewModel.updateMeetingStatus(
                            meetingId = meetingId,
                            targetStatus = MeetingStatus.IN_PROGRESS
                        )
                    })
                }
            }
        }

        MeetingControlPanel(
            timeText = "00:00:00",
            remainingTimeText = remainingTime,
            micIcon = R.drawable.mic,
            onFinish = onFinish,
            viewModel = meetingViewModel,
            isWaiting = true
        )
    }
}

@Composable
fun StartButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val baseColor = Color.Black

    Text(
        text = stringResource(R.string.start),
        style = MaterialTheme.typography.titleMedium,
        fontFamily = Pretend,
        color = if (isPressed) baseColor.copy(alpha = 0.6f) else baseColor,
        fontWeight = FontWeight.ExtraBold,
        modifier = Modifier
            .padding(top = 28.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    )
}

fun formatTargetTime(targetTimeMinutes: Int?): String {
    if (targetTimeMinutes == null) return "00:00:00"
    val totalSeconds = targetTimeMinutes * 60
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}