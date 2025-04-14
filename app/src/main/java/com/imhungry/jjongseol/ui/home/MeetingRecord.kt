package com.imhungry.jjongseol.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.imhungry.jjongseol.ui.home.meetingdata.MeetingRecord

@Composable
fun MeetingRecordsScreen() {
    var showDetails by remember { mutableStateOf(false) }
    var pagingIndex by remember { mutableStateOf(10) }
    val meetingRecordList = listOf(
        MeetingRecord("회의 1", "2025.1.2"),
        MeetingRecord("회의 2", "2025.1.2"),
        MeetingRecord("회의 3", "2026.1.2"),
        MeetingRecord("회의 4", "2026.1.2"),
        MeetingRecord("회의 1", "2022.1.2"),
        MeetingRecord("회의 2", "2022.10.25"),
        MeetingRecord("회의 3", "2022.11.20"),
        MeetingRecord("회의 4", "2022.12.27"),
        MeetingRecord("회의 5", "2023.1.2"),
        MeetingRecord("회의 6", "2024.10.6"),
        MeetingRecord("회의 1", "2022.1.2"),
        MeetingRecord("회의 2", "2022.10.25"),
        MeetingRecord("회의 3", "2022.11.20"),
        MeetingRecord("회의 4", "2022.12.27"),
        MeetingRecord("회의 5", "2023.1.2"),
        MeetingRecord("회의 6", "2024.10.6"),
        MeetingRecord("회의 1", "2025.1.2"),
        MeetingRecord("회의 2", "2025.1.2"),
        MeetingRecord("회의 3", "2026.1.2"),
        MeetingRecord("회의 4", "2026.1.2"),
        MeetingRecord("회의 1", "2022.1.2"),
        MeetingRecord("회의 2", "2022.10.25"),
        MeetingRecord("회의 3", "2022.11.20"),
        MeetingRecord("회의 4", "2022.12.27"),
        MeetingRecord("회의 5", "2023.1.2"),
        MeetingRecord("회의 6", "2024.10.6"),
        MeetingRecord("회의 1", "2022.1.2"),
        MeetingRecord("회의 2", "2022.10.25"),
        MeetingRecord("회의 3", "2022.11.20"),
        MeetingRecord("회의 4", "2022.12.27"),
        MeetingRecord("회의 5", "2023.1.2"),
        MeetingRecord("회의 6", "2024.10.6")
    )

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { showDetails = !showDetails },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "회의 기록",
                style = MaterialTheme.typography.titleLarge
            )
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = "토글 버튼",
                modifier = Modifier
                    .size(30.dp)
                    .graphicsLayer {
                        rotationZ = if (showDetails) 0f else 270f
                    }
            )
        }
        if (showDetails) {
            val currentList = meetingRecordList.take(pagingIndex)
            currentList.forEach { record ->
                MeetingRecordButton(record)
            }
            if (pagingIndex < meetingRecordList.size) {
                Text(
                    text = "더보기",
                    style = MaterialTheme.typography.bodyLarge.copy(color = Color.Gray),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable {
                            pagingIndex = (pagingIndex + 10).coerceAtMost(meetingRecordList.size)
                        }
                )
            }
        }
    }
}

@Composable
fun MeetingRecordButton(record: MeetingRecord) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { },
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = record.title,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = record.date,
            style = MaterialTheme.typography.bodyLarge.copy(color = Color.Gray)
        )
    }
}
