package com.imhungry.jjongseol.ui.meetingdetail

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.imhungry.jjongseol.viewmodel.MeetingViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun MeetingDetailEditScreen(
    meetingId: Long,
    navController: NavController,
    viewModel: MeetingViewModel = hiltViewModel()
) {

    val meeting by viewModel.selectedMeeting.collectAsState()

    LaunchedEffect(meetingId) {
        Log.d("MEETING_ID", "Loading meeting with id = $meetingId")
        viewModel.loadMeetingDetail(meetingId)
        viewModel.loadAgendas(meetingId)
    }

    if (meeting == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val safeMeeting = meeting!!
    val agendaList by viewModel.agendas.collectAsState()

    val title = remember { mutableStateOf("") }
    val location = remember { mutableStateOf("") }
    val date = remember { mutableStateOf("") }
    val startTime = remember { mutableStateOf("") }
    val endTime = remember { mutableStateOf("") }
    val totalTime = remember { mutableStateOf(60) }
    val restInterval = remember { mutableStateOf("0") }
    val restDuration = remember { mutableStateOf("0") }
    val status = remember { mutableStateOf("") }
    val isEditable = remember { mutableStateOf(false) }

    LaunchedEffect(meeting) {
        meeting?.let { safe ->
            title.value = safe.title ?: "제목 없음"
            location.value = safe.location ?: "장소 없음"

            val scheduled = safe.scheduledStartTime ?: ""
            val (rawDate, rawTime) = scheduled.split("T").let { it.getOrNull(0) to it.getOrNull(1) }

            date.value = rawDate ?: "날짜 없음"
            startTime.value = try {
                LocalDateTime.parse(scheduled)
                    .toLocalTime()
                    .format(DateTimeFormatter.ofPattern("HH:mm"))
            } catch (e: Exception) {
                "시간 없음"
            }
            try {
                endTime.value = LocalDateTime.parse(scheduled)
                    .plusMinutes(safe.targetTime.toLong())
                    .toLocalTime()
                    .format(DateTimeFormatter.ofPattern("HH:mm"))
            } catch (e: Exception) {
                endTime.value = "계산 실패"
            }
            totalTime.value = safe.targetTime
            restInterval.value = safe.restInterval.toString()
            restDuration.value = safe.restDuration.toString()
            status.value = safe.meetingStatus ?: "상태 없음"


        }
    }



    Log.d("날짜 확인","${startTime}")

    MeetingDetailScreen(
        navController = navController,
        id = safeMeeting.meetingId,
        title = title,
        location = location,
        participants = safeMeeting.participants.map { it.email },
        date = date,
        startTime = startTime,
        endTime = endTime,
        totalTime = totalTime,
        restInterval = restInterval,
        restDuration = restDuration,
        agendas = agendaList,
        status = status,
        isHost = safeMeeting.isHost,
        isEditable = isEditable.value,
        onEditClicked = { isEditable.value = true }
    )
}