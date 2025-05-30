package com.imhungry.jjongseol.ui.meeting

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.imhungry.jjongseol.R
import com.imhungry.jjongseol.ui.SilRokNavigation
import com.imhungry.jjongseol.ui.component.checklist.CheckItem
import com.imhungry.jjongseol.ui.component.dialog.ErrorDialogHandler
import com.imhungry.jjongseol.ui.component.layout.TopSheet
import com.imhungry.jjongseol.ui.meeting.bottom.MeetingControlPanel
import com.imhungry.jjongseol.viewmodel.AgendaViewModel
import com.imhungry.jjongseol.viewmodel.LoginViewModel
import com.imhungry.jjongseol.viewmodel.MeetingViewModel

@Composable
fun MeetingWaitingScreen(
    loginViewModel: LoginViewModel = hiltViewModel(),
    meetingViewModel: MeetingViewModel = hiltViewModel(),
    agendaViewModel: AgendaViewModel = hiltViewModel(),
    onFinish: (SilRokNavigation) -> Unit,
    meetingId: Long = 1L
) {
    val meetingDetail by meetingViewModel.meetingDetail.collectAsState()
    val agendas by agendaViewModel.agendaItems.collectAsState()
    val agendaError by agendaViewModel.errorMessage.collectAsState()
    val meetingError by meetingViewModel.errorMessage.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(meetingId) {
        meetingViewModel.loadMeetingDetail(meetingId)
    }

    LaunchedEffect(meetingDetail) {
        if (meetingDetail != null) {
            agendaViewModel.loadAgendas(meetingId)
        }
    }

    LaunchedEffect(meetingError, agendaError) {
        dialogMessage = meetingError ?: agendaError
        showDialog = dialogMessage != null
    }

    val firstUncheckedIndex = agendas.indexOfFirst { !it.isCompleted }
    val peekIndex = if (firstUncheckedIndex == -1) agendas.lastIndex else firstUncheckedIndex
    val isLoading = agendas.isEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ErrorDialogHandler(
            errorMessage = dialogMessage,
            showDialog = showDialog,
            onFinish = onFinish,
            clearError = { meetingViewModel.clearErrorMessage() },
            loginViewModel = loginViewModel
        )

        if (!isLoading && peekIndex in agendas.indices) {
            TopSheet(
                collapsedHeight = 60.dp,
                peekContent = {
                    CheckItem(
                        text = agendas[peekIndex].content,
                        checked = agendas[peekIndex].isCompleted,
                        isFocused = !agendas[peekIndex].isCompleted,
                        onToggle = { agendaViewModel.onToggleAgenda(peekIndex) }
                    )
                },
                content = {
                    Column {
                        agendas.forEachIndexed { i, item ->
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
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color(0xFF86CC3B))
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "회의가 시작되길 기다리는 중",
                        textAlign = TextAlign.Center,
                        color = Color.LightGray
                    )
                    StartButton(onClick = {
                        onFinish(SilRokNavigation.Meeting)
                    })
                }
            }
        }

        Divider(
            color = Color.LightGray,
            thickness = 1.dp,
            modifier = Modifier.fillMaxWidth()
        )

        MeetingControlPanel(
            timeText = "00:00:00",
            micIcon = R.drawable.inactive_mic,
            logoutIcon = R.drawable.inactive_logout,
            powerIcon = R.drawable.inactive_power,
            onFinish = onFinish,
            onExitConfirmed = {},
            viewModel = meetingViewModel,
            isWaiting = true
        )
    }
}

@Composable
fun StartButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val baseColor = Color(0xFF1E93EF)

    Text(
        text = "시작하기",
        style = MaterialTheme.typography.titleLarge,
        color = if (isPressed) baseColor.copy(alpha = 0.6f) else baseColor,
        modifier = Modifier
            .padding(top = 36.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    )
}
