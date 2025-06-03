package com.imhungry.jjongseol.ui.meeting.bottom

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.imhungry.jjongseol.R
import com.imhungry.jjongseol.data.model.meeting.MeetingStatus
import com.imhungry.jjongseol.service.MeetingSseService
import com.imhungry.jjongseol.ui.SilRokNavigation
import com.imhungry.jjongseol.ui.component.dialog.CustomDialog
import com.imhungry.jjongseol.ui.theme.Pretend
import com.imhungry.jjongseol.ui.theme.disabled
import com.imhungry.jjongseol.ui.theme.primaryTextColor
import com.imhungry.jjongseol.viewmodel.MeetingViewModel

@Composable
fun MeetingControlPanel(
    modifier: Modifier = Modifier,
    timeText: String = "00:00:00",
    remainingTimeText: String = "00:00:00",
    micIcon: Int = R.drawable.mic,
    navController: NavController,
    onFinish: (SilRokNavigation) -> Unit,
    viewModel: MeetingViewModel,
    isWaiting: Boolean = false,
    meetingId: Long? = null,
    context: Context? = null,
) {
    val micEnabled by viewModel.streamController.micEnabled.collectAsState()
    val isMicOn = if (isWaiting) false else micEnabled

    var showDialog by remember { mutableStateOf(false) }
    var showLeaveDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0x33C2C2C2)
                    )
                )
            )
    )
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .navigationBarsPadding()
            .padding(top = 12.dp, bottom = 16.dp, start = 20.dp, end = 20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = timeText,
                fontSize = 16.sp,
                fontFamily = Pretend,
                fontWeight = FontWeight.ExtraBold,
                color = if (isWaiting) disabled else primaryTextColor,
                modifier = Modifier.align(Alignment.Center)
            )
            Text(
                text = "- " + remainingTimeText,
                fontSize = 16.sp,
                fontFamily = Pretend,
                fontWeight = FontWeight.Medium,
                color = if (isWaiting) disabled else primaryTextColor,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ControlIcon(
                resId = R.drawable.power,
                description = "종료",
                enabled = !isWaiting,
                onClick = { showDialog = true },
                24.dp,
                tint = if (isWaiting) disabled else primaryTextColor
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                ControlIcon(
                    resId = if (isMicOn) R.drawable.mic else micIcon,
                    description = "마이크",
                    enabled = !isWaiting,
                    onClick = { viewModel.streamController.toggleMic(!micEnabled) },
                    28.dp,
                    tint = if (isWaiting) disabled else primaryTextColor
                )
            }

            ControlIcon(
                resId = R.drawable.out,
                description = "나가기",
                enabled = !isWaiting,
                onClick = { showLeaveDialog = true },
                24.dp,
                tint = if (isWaiting) disabled else primaryTextColor
            )
        }
    }

    if (showDialog && !isWaiting) {
        showExitDialog(
            description = "회의를 종료하시겠습니까?",
            confirmText = "종료",
            onConfirm = {
                if (meetingId != null) {
                    viewModel.updateMeetingStatus(meetingId, MeetingStatus.COMPLETED)
                }
                context?.stopService(Intent(context, MeetingSseService::class.java))
                navController.navigate("meetingRoute/end/$meetingId")
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }

    if (showLeaveDialog && !isWaiting) {
        showExitDialog(
            description = "회의에서 나가시겠습니까?",
            confirmText = "나가기",
            onConfirm = {
                viewModel.streamController.pauseEncoding()
                onFinish(SilRokNavigation.Home)
            },
            onDismiss = { showLeaveDialog = false }
        )
    }
}

@Composable
private fun showExitDialog(
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

@Composable
private fun ControlIcon(
    resId: Int,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit,
    size: Dp,
    tint: Color = Color.Unspecified
) {
    Image(
        painter = painterResource(id = resId),
        contentDescription = description,
        colorFilter = if (tint != Color.Unspecified) androidx.compose.ui.graphics.ColorFilter.tint(tint) else null,
        modifier = Modifier
            .size(size)
            .let {
                if (enabled) it.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ) else it
            }
    )
}
