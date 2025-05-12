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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.imhungry.jjongseol.R
import com.imhungry.jjongseol.data.model.agenda.AgendaUiModel
import com.imhungry.jjongseol.ui.SilRokNavigation
import com.imhungry.jjongseol.ui.component.checklist.CheckItem
import com.imhungry.jjongseol.ui.component.dialog.ErrorDialogHandler
import com.imhungry.jjongseol.ui.component.layout.TopSheet
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
    val agendaUiItems by agendaViewModel.agendaUiItems.collectAsState()
    val errorMessage by meetingViewModel.errorMessage.collectAsState()
    val showDialog = remember { mutableStateOf(false) }

    LaunchedEffect(meetingId) {
        meetingViewModel.loadMeetingDetail(meetingId)
        agendaViewModel.loadAgendas(meetingId)
    }

    val firstUncheckedIndex = agendaUiItems.indexOfFirst { !it.isChecked }
    val peekIndex = if (firstUncheckedIndex == -1) agendaUiItems.lastIndex else firstUncheckedIndex
    val isLoading = agendaUiItems.isEmpty()

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

        AgendaSection(
            items = agendaUiItems,
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
private fun AgendaSection(
    items: List<AgendaUiModel>,
    isLoading: Boolean,
    peekIndex: Int,
    firstUncheckedIndex: Int,
    onToggle: (Int) -> Unit
) {
    if (!isLoading && peekIndex in items.indices) {
        TopSheet(
            collapsedHeight = 60.dp,
            peekContent = {
                val peekItem = items[peekIndex]
                CheckItem(
                    text = peekItem.dto.content,
                    checked = peekItem.isChecked,
                    isFocused = !peekItem.isChecked,
                    onToggle = { onToggle(peekIndex) }
                )
            },
            content = {
                Column {
                    items.forEachIndexed { i, item ->
                        CheckItem(
                            text = item.dto.content,
                            checked = item.isChecked,
                            isFocused = !item.isChecked && firstUncheckedIndex == i,
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

