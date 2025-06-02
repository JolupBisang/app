package com.imhungry.jjongseol.ui.component.summary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.imhungry.jjongseol.ui.theme.Pretend
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun SummaryListItem(
    summary: String,
    timeText: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(Color.DarkGray, shape = CircleShape)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = summary,
                fontFamily = Pretend,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
        }

        Text(
            text = extractTimeOnly(timeText),
            fontFamily = Pretend,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 4.dp)
        )
    }
}

fun extractTimeOnly(isoString: String): String {
    val dt = LocalDateTime.parse(isoString)
    return dt.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
}