package com.imhungry.jjongseol.ui.meeting.bottom

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import com.imhungry.jjongseol.R
import com.imhungry.jjongseol.ui.SilRokNavigation
import com.imhungry.jjongseol.ui.component.dialog.CustomDialog
import com.imhungry.jjongseol.viewmodel.AgendaViewModel
import com.imhungry.jjongseol.viewmodel.MeetingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun MeetingControlPanel(
    modifier: Modifier = Modifier,
    timeText: String = "00:00:00",
    micIcon: Int = R.drawable.inactive_mic,
    logoutIcon: Int = R.drawable.inactive_logout,
    powerIcon: Int = R.drawable.inactive_power,
    onFinish: (SilRokNavigation) -> Unit,
    onExitConfirmed: () -> Unit,
    viewModel: MeetingViewModel,
    agendaViewModel: AgendaViewModel,
    isWaiting: Boolean = false
) {
    val context = LocalContext.current
    val micEnabled by viewModel.streamController.micEnabled.collectAsState()
    val isMicOn = if (isWaiting) false else micEnabled

    var showDialog by remember { mutableStateOf(false) }
    var showLeaveDialog by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    val iconColor = Color(0xFFB0B0B0)
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .wrapContentHeight()
            .padding(vertical = 20.dp, horizontal = 16.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(text = timeText, fontSize = 24.sp, color = iconColor)
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 18.dp)
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                ControlIcon(
                    resId = if (isMicOn) R.drawable.mic else micIcon,
                    description = "마이크",
                    enabled = true,
                    onClick = { viewModel.streamController.toggleMic(!micEnabled) }
                )
            }

            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.End) {
                ControlIcon(
                    resId = logoutIcon,
                    description = "나가기",
                    enabled = true,
                    onClick = { showLeaveDialog = true }
                )
                Spacer(modifier = Modifier.size(12.dp))
                ControlIcon(
                    resId = powerIcon,
                    description = "종료",
                    enabled = true,
                    onClick = { showDialog = true }
                )
            }
        }
    }

    if (showDialog) {
        showExitDialog(
            description = "회의를 종료하시겠습니까?",
            confirmText = "종료",
            onConfirm = {
                isSaving = true
                coroutineScope.launch {
                    saveAllAgendaStatuses(context, agendaViewModel)
                    isSaving = false
                    onExitConfirmed()
                }
            },
            onDismiss = { showDialog = false },
            isLoading = isSaving
        )
    }

    if (showLeaveDialog) {
        showExitDialog(
            description = "회의에서 나가시겠습니까?",
            confirmText = "나가기",
            onConfirm = {
                isSaving = true
                coroutineScope.launch {
                    viewModel.streamController.pauseEncoding()
                    saveAllAgendaStatuses(context, agendaViewModel)
                    isSaving = false
                    onFinish(SilRokNavigation.Home)
                }
            },
            onDismiss = { showLeaveDialog = false },
            isLoading = isSaving
        )
    }
}

@Composable
private fun showExitDialog(
    description: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean
) {
    CustomDialog(
        description = description,
        confirmText = confirmText,
        dismissText = "취소",
        onConfirmExit = onConfirm,
        onDismissRequest = onDismiss
    )
}

private suspend fun saveAllAgendaStatuses(
    context: Context,
    agendaViewModel: AgendaViewModel
) {
    context.getSharedPreferences("meeting_prefs", Context.MODE_PRIVATE)
        .edit().putBoolean("isMeetingOngoing", false).apply()

    agendaViewModel.saveAllAgendaStatusesToServer()
}

@Composable
private fun ControlIcon(
    resId: Int,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Image(
        painter = painterResource(id = resId),
        contentDescription = description,
        modifier = Modifier
            .size(36.dp)
            .let {
                if (enabled) it.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ) else it
            }
    )
}
