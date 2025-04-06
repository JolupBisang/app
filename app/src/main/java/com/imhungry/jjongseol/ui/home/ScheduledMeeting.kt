package com.imhungry.jjongseol.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ScheduledMeetingScreen() {
    var showDetails by remember { mutableStateOf(false) }
    val meetingRecords = listOf(
        "회의 1 - 2025.1.2",
        "회의 2 - 2025.1.2",
        "회의 3 - 2026.1.2",
        "회의 4 - 2026.1.2",
    )

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "예정된 회의",
                style = MaterialTheme.typography.titleLarge
            )
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = "토글 버튼",
                modifier = Modifier
                    .clickable { showDetails = !showDetails }
                    .size(30.dp)
                    .graphicsLayer {
                        rotationZ = if (showDetails) 0f else 270f
                    }
            )
        }
        if (showDetails) {
            meetingRecords.forEach { record ->
                ScheduledMeetingButton(record)
            }
        }
    }
}

@Composable
fun ScheduledMeetingButton(record: String) {
    Text(
        text = buildAnnotatedString { append(record) },
        style = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { }
    )
}
