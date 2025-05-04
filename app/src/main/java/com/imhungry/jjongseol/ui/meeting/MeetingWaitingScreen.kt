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
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.imhungry.jjongseol.R
import com.imhungry.jjongseol.data.model.agenda.AgendaDto
import com.imhungry.jjongseol.ui.SilRokNavigation
import com.imhungry.jjongseol.ui.component.CheckItem
import com.imhungry.jjongseol.ui.component.CustomDialog
import com.imhungry.jjongseol.ui.component.TopSheet
import com.imhungry.jjongseol.ui.meeting.bottom.MeetingControlPanel
import com.imhungry.jjongseol.viewmodel.AgendaViewModel
import com.imhungry.jjongseol.viewmodel.MeetingViewModel

@Composable
fun MeetingWaitingScreen(
    meetingViewModel: MeetingViewModel = hiltViewModel(),
    agendaViewModel: AgendaViewModel = hiltViewModel(),
    onFinish: (SilRokNavigation) -> Unit,
    meetingId: Long = 1L
) {
    val agendas by agendaViewModel.agendaItems.collectAsState()
    val checkedStates by agendaViewModel.checkedStates.collectAsState()
    val errorMessage by meetingViewModel.errorMessage.collectAsState()
    val showDialog = remember { mutableStateOf(false) }

    // 아젠다 로딩 시작
    LaunchedEffect(meetingId) {
        agendaViewModel.onError = {
            meetingViewModel.setErrorMessage(it)
        }
        agendaViewModel.loadAgendas(meetingId)
    }

    val firstUncheckedIndex = checkedStates.indexOfFirst { !it }
    val peekIndex = if (firstUncheckedIndex == -1) checkedStates.lastIndex else firstUncheckedIndex
    val isLoading = agendas.isEmpty() || checkedStates.isEmpty() || peekIndex !in agendas.indices

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ErrorDialogSection(errorMessage, showDialog, onFinish, meetingViewModel)

        AgendaSection(
            agendas = agendas,
            checkedStates = checkedStates,
            isLoading = isLoading,
            peekIndex = peekIndex,
            firstUncheckedIndex = firstUncheckedIndex,
            onToggle = { agendaViewModel.toggleAgendaChecked(it) }
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            MeetingContentSection(isLoading = isLoading, onStart = { onFinish(SilRokNavigation.Meeting) })
        }

        Divider(
            color = Color.LightGray,
            thickness = 1.dp,
            modifier = Modifier.fillMaxWidth()
        )

        MeetingControlPanel(
            timeText = "00:00:00",
            micEnabled = false,
            micIcon = R.drawable.inactive_mic,
            logoutIcon = R.drawable.inactive_logout,
            powerIcon = R.drawable.inactive_power,
            onFinish = onFinish,
            onExitConfirmed = {},
            viewModel = meetingViewModel
        )
    }
}

@Composable
private fun ErrorDialogSection(
    errorMessage: String?,
    showDialog: MutableState<Boolean>,
    onFinish: (SilRokNavigation) -> Unit,
    meetingViewModel: MeetingViewModel
) {
    if (errorMessage != null) showDialog.value = true

    if (showDialog.value && errorMessage != null) {
        CustomDialog(
            description = if (errorMessage == "TOKEN_EXPIRED")
                "로그인 정보가 만료되었어요. 다시 로그인해주세요."
            else errorMessage,
            confirmText = if (errorMessage == "TOKEN_EXPIRED") "로그인 하기" else "홈으로",
            showDismissButton = false,
            onDismissRequest = {},
            onConfirmExit = {
                showDialog.value = false
                meetingViewModel.clearErrorMessage()
                val destination = if (errorMessage == "TOKEN_EXPIRED") SilRokNavigation.Login else SilRokNavigation.Home
                onFinish(destination)
            }
        )
    }
}

@Composable
private fun AgendaSection(
    agendas: List<AgendaDto>,
    checkedStates: List<Boolean>,
    isLoading: Boolean,
    peekIndex: Int,
    firstUncheckedIndex: Int,
    onToggle: (Int) -> Unit
) {
    if (!isLoading && peekIndex in agendas.indices) {
        TopSheet(
            collapsedHeight = 60.dp,
            peekContent = {
                CheckItem(
                    text = agendas[peekIndex].content,
                    checked = checkedStates[peekIndex],
                    isFocused = !checkedStates[peekIndex],
                    onToggle = { onToggle(peekIndex) }
                )
            },
            content = {
                Column {
                    agendas.forEachIndexed { i, item ->
                        CheckItem(
                            text = item.content,
                            checked = checkedStates[i],
                            isFocused = !checkedStates[i] && firstUncheckedIndex == i,
                            onToggle = { onToggle(i) }
                        )
                    }
                }
            }
        )
    }
}

@Composable
private fun MeetingContentSection(
    isLoading: Boolean,
    onStart: () -> Unit
) {
    if (isLoading) {
        CircularProgressIndicator(color = Color(0xFF86CC3B))
    } else {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "회의가 시작되길\n기다리는 중",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                color = Color.LightGray
            )
            StartButton(onClick = onStart)
        }
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

