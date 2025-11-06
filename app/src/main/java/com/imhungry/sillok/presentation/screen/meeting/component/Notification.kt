package com.imhungry.sillok.presentation.screen.meeting.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imhungry.sillok.presentation.state.meeting.FeedbackUi
import com.imhungry.sillok.ui.theme.blurBackground
import com.imhungry.sillok.ui.theme.gray500
import com.imhungry.sillok.ui.theme.green500
import com.imhungry.sillok.ui.theme.orange100

@Composable
fun Notification(
    feedback: FeedbackUi,
    modifier: Modifier = Modifier,
    blur: Boolean = false,
    isRead: Boolean,
    onTimeClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .background(
                color = if (blur) blurBackground else green500,
                shape = MaterialTheme.shapes.small
            )
            .border(
                width = 1.dp,
                color = gray500,
                shape = MaterialTheme.shapes.small
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
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

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(top = 12.dp, bottom = 12.dp, end = 20.dp),
                horizontalAlignment = Alignment.End
            ) {
                if (!isRead) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(orange100, shape = CircleShape)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = feedback.timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = onTimeClick != null
                    ) { onTimeClick?.invoke() }
                        .padding(bottom = 3.dp)
                )
            }
        }
    }
}
