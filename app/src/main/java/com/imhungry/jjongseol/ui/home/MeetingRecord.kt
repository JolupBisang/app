package com.imhungry.jjongseol.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp

@Composable
fun MeetingRecordsScreen() {
    var showDetails by remember { mutableStateOf(false) }
    val meetingRecords = listOf(
        "회의 1 - 2022.1.2",
        "회의 2 - 2022.10.25",
        "회의 3 - 2022.11.20",
        "회의 4 - 2022.12.27",
        "회의 5 - 2023.1.2",
        "회의 6 - 2024.10.6"
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
                text = "회의 기록",
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
                MeetingRecordButton(record)
            }
        }
    }
}

@Composable
fun MeetingRecordButton(record: String) {
    Text(
        text = buildAnnotatedString { append(record) },
        style = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { }
    )
}
