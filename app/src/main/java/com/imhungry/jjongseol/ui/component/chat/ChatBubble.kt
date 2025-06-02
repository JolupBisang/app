package com.imhungry.jjongseol.ui.component.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.imhungry.jjongseol.data.model.chat.ChatMessage
import java.time.LocalTime

@Composable
fun ChatBubble(chatMessage: ChatMessage) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalAlignment = if (chatMessage.isMe) Alignment.End else Alignment.Start
    ) {
        if (chatMessage.isMe) {
            MyMessage(chatMessage)
        } else {
            OthersMessage(chatMessage)
        }
    }
}

@Composable
private fun MyMessage(chatMessage: ChatMessage) {
    Column(horizontalAlignment = Alignment.End) {
        ChatBox(
            text = chatMessage.message,
            backgroundColor = Color(0xFFB9B9B9),
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 12.dp, bottomEnd = 4.dp)
        )

        TimestampText(
            time = chatMessage.timestamp,
            modifier = Modifier.padding(top = 2.dp, end = 1.dp)
        )
    }
}

@Composable
private fun OthersMessage(chatMessage: ChatMessage) {
    Row(verticalAlignment = Alignment.Top) {
        Image(
            painter = rememberAsyncImagePainter(chatMessage.profileImageUrl),
            contentDescription = "profile",
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .border(0.5.dp, Color(0xFFE4E4E4), CircleShape)
        )

        Spacer(modifier = Modifier.width(6.dp))

        ChatBox(
            text = chatMessage.message,
            backgroundColor = Color(0xFFEEEEEF),
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 4.dp, bottomEnd = 12.dp)
        )
    }

    TimestampText(
        time = chatMessage.timestamp,
        modifier = Modifier.padding(start = 45.dp, top = 2.dp)
    )
}

@Composable
private fun ChatBox(
    text: String,
    backgroundColor: Color,
    shape: RoundedCornerShape
) {
    Box(
        modifier = Modifier
            .background(backgroundColor, shape = shape)
            .widthIn(max = LocalConfiguration.current.screenWidthDp.dp * 0.67f)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun TimestampText(
    time: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 11.sp
) {
    Text(
        text = time,
        fontSize = fontSize,
        modifier = modifier
    )
}
