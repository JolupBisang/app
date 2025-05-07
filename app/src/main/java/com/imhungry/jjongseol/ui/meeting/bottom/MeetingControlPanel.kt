package com.imhungry.jjongseol.ui.meeting.bottom

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imhungry.jjongseol.R
import com.imhungry.jjongseol.ui.SilRokNavigation
import com.imhungry.jjongseol.ui.component.CustomDialog
import com.imhungry.jjongseol.viewmodel.MeetingViewModel

@Composable
fun MeetingControlPanel(
    modifier: Modifier = Modifier,
    timeText: String = "00:00:00",
    micEnabled: Boolean = false,
    onMicToggle: ((Boolean) -> Unit)? = null,
    micIcon: Int = R.drawable.inactive_mic,
    logoutIcon: Int = R.drawable.inactive_logout,
    powerIcon: Int = R.drawable.inactive_power,
    onFinish: (SilRokNavigation) -> Unit,
    onExitConfirmed: () -> Unit,
    viewModel: MeetingViewModel
) {
    var isMicOn by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }
    var showLeaveDialog by remember { mutableStateOf(false) }

    val iconColor = Color(0xFFB0B0B0)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .wrapContentHeight()
            .padding(vertical = 20.dp, horizontal = 16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = timeText,
                fontSize = 24.sp,
                color = iconColor
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp)
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                ControlIcon(
                    resId = if (micEnabled && isMicOn) R.drawable.mic else micIcon,
                    description = "마이크",
                    enabled = micEnabled,
                    onClick = {
                        isMicOn = !isMicOn
                        onMicToggle?.invoke(isMicOn)
                    }
                )
            }

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.End
            ) {
                ControlIcon(
                    resId = logoutIcon,
                    description = "나가기",
                    enabled = micEnabled,
                    onClick = { showLeaveDialog = true }
                )

                Spacer(modifier = Modifier.size(12.dp))

                ControlIcon(
                    resId = powerIcon,
                    description = "종료",
                    enabled = micEnabled,
                    onClick = { showDialog = true }
                )
            }
        }
    }

    if (showDialog) {
        ExitDialog(
            description = "회의를 종료하시겠습니까?",
            confirmText = "종료",
            onConfirm = {
                showDialog = false
                onExitConfirmed()
                onFinish(SilRokNavigation.MeetingEnd)
            },
            onDismiss = { showDialog = false }
        )
    }

    if (showLeaveDialog) {
        ExitDialog(
            description = "회의에서 나가시겠습니까?",
            confirmText = "나가기",
            onConfirm = {
                showLeaveDialog = false
                viewModel.pauseEncoding()
                onFinish(SilRokNavigation.Home)
            },
            onDismiss = { showLeaveDialog = false }
        )
    }
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
            .let { if (enabled) it.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ) else it }
    )
}

@Composable
private fun ExitDialog(
    description: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    CustomDialog(
        description = description,
        confirmText = confirmText,
        dismissText = "취소",
        onConfirmExit = onConfirm,
        onDismissRequest = onDismiss
    )
}

