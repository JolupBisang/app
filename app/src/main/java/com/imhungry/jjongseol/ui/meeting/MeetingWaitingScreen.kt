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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.imhungry.jjongseol.viewmodel.MeetingViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun MeetingWaitingScreen(
    meetingViewModel: MeetingViewModel = hiltViewModel(),
    agendaViewModel: AgendaViewModel = hiltViewModel(),
    onFinish: (SilRokNavigation) -> Unit,
    meetingId: Long = 1L
) {
    val agendas by agendaViewModel.agendaItems.collectAsState()
    val errorMessage by meetingViewModel.errorMessage.collectAsState()
    val showDialog = remember { mutableStateOf(false) }
    val checkedStates = remember(agendas) { mutableStateListOf<Boolean>().apply { addAll(agendas.map { it.isCompleted }) } }
    val isSaving = remember { mutableStateOf(false) }

    LaunchedEffect(meetingId) {
        val success = meetingViewModel.loadMeetingDetail(meetingId)
        if (success) {
            agendaViewModel.loadAgendas(meetingId)
        }
    }

    val firstUncheckedIndex = checkedStates.indexOfFirst { !it }
    val peekIndex = if (firstUncheckedIndex == -1) checkedStates.lastIndex else firstUncheckedIndex
    val isLoading = agendas.isEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ErrorDialogHandler(
            errorMessage = errorMessage,
            showDialog = showDialog,
            onFinish = onFinish,
            clearError = { meetingViewModel.clearErrorMessage() }
        )

        if (!isLoading && peekIndex in agendas.indices) {
            TopSheet(
                collapsedHeight = 60.dp,
                peekContent = {
                    CheckItem(
                        text = agendas[peekIndex].content,
                        checked = checkedStates[peekIndex],
                        isFocused = !checkedStates[peekIndex],
                        onToggle = { checkedStates[peekIndex] = !checkedStates[peekIndex] }
                    )
                },
                content = {
                    Column {
                        agendas.forEachIndexed { i, item ->
                            CheckItem(
                                text = item.content,
                                checked = checkedStates[i],
                                isFocused = !checkedStates[i] && firstUncheckedIndex == i,
                                onToggle = { checkedStates[i] = !checkedStates[i] }
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
                        text = "회의가 시작되길\n기다리는 중",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        color = Color.LightGray
                    )
                    StartButton(
                        onClick = {
                            isSaving.value = true
                            CoroutineScope(Dispatchers.IO).launch {
                                agendas.forEachIndexed { index, agenda ->
                                    agendaViewModel.saveAgendaCompletionStatusToServer(
                                        agendaId = agenda.agendaId,
                                        isCompleted = checkedStates[index]
                                    )
                                }

                                launch(Dispatchers.Main) {
                                    isSaving.value = false
                                    onFinish(SilRokNavigation.Meeting)
                                }
                            }
                        }
                    )
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
            agendaViewModel = agendaViewModel,
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
