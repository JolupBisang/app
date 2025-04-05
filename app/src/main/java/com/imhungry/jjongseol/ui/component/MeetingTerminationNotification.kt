package com.imhungry.jjongseol.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun MeetingTerminationNotification(
    totalDurationMillis: Long = 50_000L,
    onDismiss: () -> Unit
) {
    var progress by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        val startTime = withFrameNanos { it }
        while (progress < 1f) {
            val currentTime = withFrameNanos { it }
            val elapsed = currentTime - startTime
            progress = (elapsed / totalDurationMillis.toFloat() / 1_000_000f).coerceIn(0f, 1f)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFE64646))
                .background(Color(0xFFFFF5F5))
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "이 회의는 곧 종료됩니다!",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Red,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "계속 회의하기",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF1D90EA),
                    modifier = Modifier.clickable { onDismiss() }
                )
                Text(
                    text = "취소하기",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF1D90EA),
                    modifier = Modifier.clickable { onDismiss() }
                )
            }
        }

        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp),
            color = Color(0xA8E64646),
        )
    }
}

