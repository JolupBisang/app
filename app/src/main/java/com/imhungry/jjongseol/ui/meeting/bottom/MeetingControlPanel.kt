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

@Composable
fun MeetingControlPanel(
    modifier: Modifier = Modifier,
) {
    var isMicOn by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .weight(0.35f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "00:03:11",
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
                modifier = Modifier
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = if (isMicOn) R.drawable.mic else R.drawable.micoff),
                    contentDescription = if (isMicOn) "마이크 켜짐" else "마이크 꺼짐",
                    modifier = Modifier
                        .size(36.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            isMicOn = !isMicOn
                        }
                )
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logout),
                    contentDescription = "나가기",
                    modifier = Modifier
                        .size(36.dp)
                )

                Spacer(modifier = Modifier.size(12.dp))

                Image(
                    painter = painterResource(id = R.drawable.power),
                    contentDescription = "종료",
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}