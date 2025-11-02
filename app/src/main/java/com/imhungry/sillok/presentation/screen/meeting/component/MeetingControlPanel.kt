package com.imhungry.sillok.presentation.screen.meeting.component

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.imhungry.sillok.R
import com.imhungry.sillok.presentation.viewmodel.meeting.MeetingInProgressViewModel
import com.imhungry.sillok.ui.theme.primaryTextColor
import com.imhungry.sillok.ui.theme.shadow
import com.imhungry.sillok.ui.theme.tertiary
import com.imhungry.sillok.ui.theme.whiteBackground

@Composable
fun MeetingControlPanel(
    modifier: Modifier = Modifier,
    timeText: String = "00:00:00",
    remainingTimeText: String = "00:00:00",
    onBack: () -> Unit,
    onComplete: () -> Unit,
    meetingInProgressViewModel: MeetingInProgressViewModel,
) {
    val micEnabled by meetingInProgressViewModel.micEnabled.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        shadow
                    )
                )
            )
    )
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(whiteBackground)
            .padding(start = 20.dp, end = 20.dp, top = 12.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = timeText,
                style = MaterialTheme.typography.titleMedium,
                color = primaryTextColor,
                modifier = Modifier.align(Alignment.Center)
            )
            Text(
                text = "- $remainingTimeText",
                style = MaterialTheme.typography.bodySmall,
                color = tertiary,
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
                onClick = onComplete,
                size = 24.dp
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
//                ControlIcon(
//                    resId = if (micEnabled) R.drawable.mic else R.drawable.micoff,
//                    description = "마이크",
//                    onClick = {},
//                    size = 28.dp
//                )
                ControlIcon(
                    resId = if (micEnabled) R.drawable.mic else R.drawable.micoff,
                    description = "마이크",
                    onClick = { meetingInProgressViewModel.toggleMic() },
                    size = 28.dp
                )
            }

            ControlIcon(
                resId = R.drawable.out,
                description = "나가기",
                onClick = onBack,
                size = 24.dp
            )
        }
    }
}

@Composable
private fun ControlIcon(
    resId: Int,
    description: String,
    onClick: () -> Unit,
    size: Dp
) {
    Image(
        painter = painterResource(id = resId),
        contentDescription = description,
        modifier = Modifier
            .size(size)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    )
}
