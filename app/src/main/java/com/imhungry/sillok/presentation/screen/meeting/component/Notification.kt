package com.imhungry.sillok.presentation.screen.meeting.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imhungry.sillok.presentation.state.meeting.FeedbackUi
import com.imhungry.sillok.ui.theme.gray500
import com.imhungry.sillok.ui.theme.orange100

@Composable
fun  Notification(
    feedback: FeedbackUi,
    modifier: Modifier = Modifier,
    blur: Boolean = false,
    onTimeClick: (() -> Unit)? = null
) {
    AnimatedVisibility(
        visible = feedback.isRead,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = modifier
                .background(
                    color = if(blur) Color(0xD9E0E0E0) else gray500,
                    shape = MaterialTheme.shapes.medium
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = feedback.comment,
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 14.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(top = 8.dp, bottom = 8.dp, end = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = feedback.timestamp,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            enabled = onTimeClick != null
                        ) { onTimeClick?.invoke() },
                    )

                    if (!feedback.isRead) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(orange100, shape = CircleShape)
                                .align(Alignment.TopEnd)
                        )
                    }
                }
            }
        }
    }
}
