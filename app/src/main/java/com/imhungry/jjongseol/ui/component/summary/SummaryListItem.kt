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
import com.imhungry.jjongseol.ui.theme.primaryTextColor
import com.imhungry.jjongseol.ui.theme.tertiary
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
                    .size(3.dp)
                    .background(primaryTextColor, shape = CircleShape)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = summary,
                fontFamily = Pretend,
                fontWeight = FontWeight.Medium,
                color = primaryTextColor,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = timeText,
                fontFamily = Pretend,
                fontWeight = FontWeight.Medium,
                color = tertiary,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
