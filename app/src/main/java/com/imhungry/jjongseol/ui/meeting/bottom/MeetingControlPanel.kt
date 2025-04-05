package com.imhungry.jjongseol.ui.meeting.bottom

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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

@Composable
fun MeetingControlPanel(
    modifier: Modifier = Modifier,
    timeText: String = "00:00:00",
    micEnabled: Boolean = false,
    onMicToggle: (() -> Unit)? = null,
    micIcon: Int = R.drawable.inactive_mic,
    logoutIcon: Int = R.drawable.inactive_logout,
    powerIcon: Int = R.drawable.inactive_power,
    onFinish: (SilRokNavigation) -> Unit,
    onExitConfirmed: () -> Unit
) {
    var isMicOn by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }
    var showLeaveDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .weight(0.35f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = timeText,
                fontSize = 24.sp,
                color = Color(0xFFB0B0B0)
            )
        }

        Row(
            modifier = Modifier
                .weight(0.65f)
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(top = 8.dp)
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(
                        id = if (micEnabled && isMicOn) R.drawable.mic else micIcon
                    ),
                    contentDescription = "마이크",
                    modifier = Modifier
                        .size(36.dp)
                        .let { baseModifier ->
                            if (micEnabled) {
                                baseModifier.clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    isMicOn = !isMicOn
                                    onMicToggle?.invoke()
                                }
                            } else baseModifier
                        }
                )
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Image(
                    painter = painterResource(id = logoutIcon),
                    contentDescription = "나가기",
                    modifier = Modifier
                        .size(36.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (micEnabled) {
                                showLeaveDialog = true
                            }
                        }
                )

                Spacer(modifier = Modifier.size(12.dp))

                Image(
                    painter = painterResource(id = powerIcon),
                    contentDescription = "종료",
                    modifier = Modifier
                        .size(36.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (micEnabled) {
                                showDialog = true
                            }
                        }
                )
            }
        }
    }

    if (showDialog) {
        CustomDialog(
            description = "회의를 종료하시겠습니까?",
            confirmText = "종료",
            dismissText = "취소",
            onDismissRequest = { showDialog = false },
            onConfirmExit = {
                showDialog = false
                onExitConfirmed()
                onFinish(SilRokNavigation.MeetingEnd)
            }
        )
    }

    if (showLeaveDialog) {
        CustomDialog(
            description = "회의에서 나가시겠습니까?",
            confirmText = "나가기",
            dismissText = "취소",
            onDismissRequest = { showLeaveDialog = false },
            onConfirmExit = {
                showLeaveDialog = false
                onFinish(SilRokNavigation.Home)
            }
        )
    }
}
