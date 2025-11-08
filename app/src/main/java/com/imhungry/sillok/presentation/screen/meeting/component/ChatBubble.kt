package com.imhungry.sillok.presentation.screen.meeting.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.imhungry.sillok.presentation.state.meeting.SegmentUi
import com.imhungry.sillok.ui.theme.brown200
import com.imhungry.sillok.ui.theme.brown500
import com.imhungry.sillok.ui.theme.green300
import com.imhungry.sillok.ui.theme.green500
import com.imhungry.sillok.ui.theme.placeHolder
import com.imhungry.sillok.ui.theme.tertiary

@Composable
fun ChatBubble(
    segment: SegmentUi,
    highlighted: Boolean = false,
    onSegmentClick: ((String) -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 3.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onSegmentClick?.invoke(segment.timestamp) }
            ),
        horizontalAlignment = if (segment.isFromCurrentUser) Alignment.End else Alignment.Start
    ) {
        if (segment.isFromCurrentUser) {
            MyMessage(segment, highlighted)
        } else {
            OthersMessage(segment, highlighted)
        }
    }
}

@Composable
private fun MyMessage(segment: SegmentUi, highlighted: Boolean) {
    Column(horizontalAlignment = Alignment.End) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.Bottom
        ) {
            if (!segment.isSameAsNext) {
                TimestampText(
                    time = segment.timestamp,
                    modifier = Modifier
                        .align(Alignment.Bottom)
                        .padding(bottom = 2.dp, end = 1.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            ChatBox(
                text = segment.text,
                backgroundColor = if (highlighted) green300 else green500,
                shape = RoundedCornerShape(
                    topStart = 10.dp,
                    topEnd = 10.dp,
                    bottomStart = 10.dp,
                    bottomEnd = 4.dp
                ),
            )
        }
    }
}

@Composable
private fun OthersMessage(
    segment: SegmentUi,
    highlighted: Boolean
) {
    Row(verticalAlignment = Alignment.Top) {
        if (!segment.isSameAsPrevious) {
            AsyncImage(
                model = segment.profileImage.ifEmpty { null },
                contentDescription = "프로필 이미지",
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(placeHolder),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(6.dp))

            Column {
                Text(
                    text = segment.nickname,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(verticalAlignment = Alignment.Bottom) {
                    ChatBox(
                        text = segment.text,
                        backgroundColor = if (highlighted) brown200 else brown500,
                        shape = RoundedCornerShape(
                            topStart = 10.dp,
                            topEnd = 10.dp,
                            bottomStart = 4.dp,
                            bottomEnd = 10.dp
                        ),
                    )
                    if (!segment.isSameAsNext) {
                        Spacer(modifier = Modifier.width(2.dp))
                        TimestampText(
                            time = segment.timestamp,
                            modifier = Modifier
                                .align(Alignment.Bottom)
                                .padding(bottom = 2.dp)
                        )
                    }
                }
            }
        } else {
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.padding(start = 43.dp)
            ) {
                ChatBox(
                    text = segment.text,
                    backgroundColor = brown500,
                    shape = RoundedCornerShape(
                        topStart = 14.dp,
                        topEnd = 14.dp,
                        bottomStart = 4.dp,
                        bottomEnd = 14.dp
                    ),
                )
                if (!segment.isSameAsNext) {
                    Spacer(modifier = Modifier.width(4.dp))
                    TimestampText(
                        time = segment.timestamp,
                        modifier = Modifier
                            .align(Alignment.Bottom)
                            .padding(bottom = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatBox(
    text: String,
    backgroundColor: Color,
    shape: RoundedCornerShape,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(backgroundColor, shape = shape)
            .widthIn(max = LocalConfiguration.current.screenWidthDp.dp * 0.64f)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
private fun TimestampText(
    time: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = time,
        fontWeight = FontWeight.Normal,
        style = MaterialTheme.typography.labelSmall,
        fontSize = 12.sp,
        color = tertiary,
        modifier = modifier
    )
}