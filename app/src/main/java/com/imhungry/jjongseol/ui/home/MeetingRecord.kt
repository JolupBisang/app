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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.imhungry.jjongseol.ui.home.meetingdata.MeetingRecordData
import com.imhungry.jjongseol.viewmodel.MeetingViewModel

@Composable
fun MeetingRecordsScreen(navController: NavController,  viewModel: MeetingViewModel = hiltViewModel()) {
    var showDetails by remember { mutableStateOf(false) }
    var pagingIndex by remember { mutableStateOf(10) }

    val pastMeetings by viewModel.pastMeetings.collectAsState()
    val currentList = pastMeetings.take(pagingIndex)

    LaunchedEffect(Unit) {
        if (pastMeetings.isEmpty()) {
            viewModel.resetMonthOffsets()
            viewModel.loadMeetings()
        }
    }

    Column(
        modifier = Modifier
            .padding(6.dp)
            .padding(bottom = 150.dp)
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
            Text("지난 회의", color = Color.Black, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = "토글 버튼",
                modifier = Modifier
                    .size(30.dp)
                    .graphicsLayer { rotationZ = if (showDetails) 0f else 270f }
            )
        }

        if (showDetails) {
            currentList.forEach {
                MeetingRecordButton(record = MeetingRecordData(it.title, it.startDateTime.toLocalDate().toString())) {
                    navController.navigate("meetingDetail/${it.id}")
                }
            }

            //고민
            //if (pagingIndex < pastMeetings.size) {
                Text(
                    text = "지난 달 기록된 회의 더보기",
                    style = MaterialTheme.typography.bodyLarge.copy(color = Color.Gray),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable {
                            pagingIndex += 10
                            if (pagingIndex >= pastMeetings.size) {
                                viewModel.loadMorePastMeetings()
                            }
                        }
                )
            //}
        }
    }
}

@Composable
fun MeetingRecordButton(record: MeetingRecordData, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 10.dp)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "∘ "+record.title,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = record.date,
            style = MaterialTheme.typography.bodyLarge.copy(color = Color.Gray)
        )
    }
}
