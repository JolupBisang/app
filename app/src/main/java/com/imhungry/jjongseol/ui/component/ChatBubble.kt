package com.imhungry.jjongseol.ui.component

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.imhungry.jjongseol.data.chat.ChatMessage

@Composable
fun ChatBubble(chatMessage: ChatMessage) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
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
            backgroundColor = Color(0xFFDCF8C6),
            shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomStart = 14.dp, bottomEnd = 4.dp)
        )

        TimestampText(
            time = chatMessage.timestamp,
            modifier = Modifier.padding(top = 2.dp, end = 1.dp),
            fontSize = 9.sp
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
                .border(0.5.dp, Color.LightGray, CircleShape)
        )

        Spacer(modifier = Modifier.width(6.dp))

        Column {
            Text(
                text = chatMessage.sender,
                style = MaterialTheme.typography.labelMedium.copy(color = Color.Black),
                modifier = Modifier.padding(top = 1.dp, bottom = 2.dp)
            )

            ChatBox(
                text = chatMessage.message,
                backgroundColor = Color(0xFFF5F5F5),
                shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomStart = 4.dp, bottomEnd = 14.dp)
            )
        }
    }

    TimestampText(
        time = chatMessage.timestamp,
        modifier = Modifier.padding(start = 45.dp, top = 2.dp),
        fontSize = 10.sp
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
    fontSize: androidx.compose.ui.unit.TextUnit = 10.sp
) {
    Text(
        text = time,
        fontSize = fontSize,
        color = Color.Gray,
        modifier = modifier
    )
}
