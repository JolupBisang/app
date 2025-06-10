package com.imhungry.jjongseol.ui.meeting.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import coil.compose.rememberAsyncImagePainter
import com.imhungry.jjongseol.R
import com.imhungry.jjongseol.data.model.segment.DiarizedSegment
import com.imhungry.jjongseol.ui.theme.Pretend
import com.imhungry.jjongseol.ui.theme.blackColor
import com.imhungry.jjongseol.ui.theme.brown500
import com.imhungry.jjongseol.ui.theme.green500
import com.imhungry.jjongseol.ui.theme.primaryTextColor
import com.imhungry.jjongseol.ui.theme.tertiary
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun ChatBubble(diarizedSegment: DiarizedSegment, nickname: String, isMe : Boolean, time: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        if (isMe) {
            MyMessage(diarizedSegment, time)
        } else {
            OthersMessage(diarizedSegment, nickname, time)
        }
    }
}

@Composable
private fun MyMessage(diarizedSegment: DiarizedSegment, time: String) {
    Column(horizontalAlignment = Alignment.End) {
        ChatBox(
            text = diarizedSegment.text,
            backgroundColor = green500,
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 12.dp, bottomEnd = 4.dp)
        )

        TimestampText(
            time = time,
            modifier = Modifier.padding(top = 2.dp, end = 1.dp)
        )
    }
}

val profileDrawables = listOf(
    R.drawable.profile1,
    R.drawable.profile2,
    R.drawable.profile3,
    R.drawable.profile4,
    R.drawable.profile5,
    R.drawable.profile6,
    R.drawable.profile7,
    R.drawable.profile8,
    R.drawable.profile9,
    R.drawable.profile10,
    R.drawable.profile11
)

@Composable
private fun OthersMessage(diarizedSegment: DiarizedSegment, nickname: String, time: String) {
    val profileIndex = (diarizedSegment.userId % profileDrawables.size).toInt()
    val profileRes = profileDrawables[profileIndex]

    Row(verticalAlignment = Alignment.Top) {
        Image(
            painter = rememberAsyncImagePainter(profileRes),
            contentDescription = "profile",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .border(0.5.dp, blackColor, CircleShape)
        )

        Spacer(modifier = Modifier.width(6.dp))

        Column {
            Text(
                text = nickname,
                fontFamily = Pretend,
                style = MaterialTheme.typography.titleSmall.copy(color = Color.Black),
                modifier = Modifier.padding(top = 1.dp, bottom = 2.dp)
            )

            ChatBox(
                text = diarizedSegment.text,
                backgroundColor = brown500,
                shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomStart = 4.dp, bottomEnd = 14.dp)
            )
        }
    }

    TimestampText(
        time = time,
        modifier = Modifier.padding(start = 47.dp, top = 2.dp)
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
            fontFamily = Pretend,
            fontWeight = FontWeight.Medium,
            color = primaryTextColor,
            style = MaterialTheme.typography.bodyMedium
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
        fontFamily = Pretend,
        fontWeight = FontWeight.Medium,
        style = MaterialTheme.typography.labelSmall,
        color = tertiary,
        modifier = modifier
    )
}

/*fun formatKoreanTime(isoString: String): String {
    return try {
        val dateTime = LocalDateTime.parse(isoString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val hour24 = dateTime.hour
        val minute = dateTime.minute

        val amPm = if (hour24 < 12) "오전" else "오후"
        val hour12 = when {
            hour24 == 0 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }
        "$amPm ${hour12}시 ${minute}분"
    } catch (e: Exception) {
        ""
    }
}*/
