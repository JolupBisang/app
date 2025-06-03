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
    val agendaList by viewModel.agendas.collectAsState()

    val showCancelDialog = remember { mutableStateOf(false) }

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

    // 원본 복제본
    val originalTitle = remember { mutableStateOf("") }
    val originalLocation = remember { mutableStateOf("") }
    val originalDate = remember { mutableStateOf("") }
    val originalStartTime = remember { mutableStateOf("") }
    val originalEndTime = remember { mutableStateOf("") }
    val originalTotalTime = remember { mutableStateOf(60) }
    val originalRestInterval = remember { mutableStateOf("0") }
    val originalRestDuration = remember { mutableStateOf("0") }
    val originalAgendas = remember { mutableStateListOf<String>() }
    val originalParticipants = remember { mutableStateListOf<String>() }

    if (meeting == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val participants = remember { mutableStateListOf(*safeMeeting.participants.map { it.email }.toTypedArray()) }

    LaunchedEffect(safeMeeting) {
        title.value = safeMeeting.title ?: ""
        location.value = safeMeeting.location ?: ""
        val (d, t) = safeMeeting.scheduledStartTime?.split("T") ?: listOf("", "")
        date.value = d
        startTime.value = t.take(5)
        endTime.value = LocalDateTime.parse(safeMeeting.scheduledStartTime)
            .plusMinutes(safeMeeting.targetTime.toLong())
            .toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))
        totalTime.value = safeMeeting.targetTime
        restInterval.value = safeMeeting.restInterval.toString()
        restDuration.value = safeMeeting.restDuration.toString()
        status.value = safeMeeting.meetingStatus ?: ""

        originalTitle.value = title.value
        originalLocation.value = location.value
        originalDate.value = date.value
        originalStartTime.value = startTime.value
        originalEndTime.value = endTime.value
        originalTotalTime.value = totalTime.value
        originalRestInterval.value = restInterval.value
        originalRestDuration.value = restDuration.value
        originalAgendas.clear()
        originalAgendas.addAll(agendaList)
        originalParticipants.clear()
        originalParticipants.addAll(participants)
    }

    val hasChanges: () -> Boolean = {
        title.value != originalTitle.value ||
                location.value != originalLocation.value ||
                date.value != originalDate.value ||
                startTime.value != originalStartTime.value ||
                endTime.value != originalEndTime.value ||
                totalTime.value != originalTotalTime.value ||
                restInterval.value != originalRestInterval.value ||
                restDuration.value != originalRestDuration.value ||
                agendaList != originalAgendas ||
                participants != originalParticipants
    }

    val onConfirmEdit: () -> Unit = {
        val fullStartTime = "${date.value}T${startTime.value}"
        viewModel.confirmEdit(
            id = safeMeeting.meetingId,
            title = title.value,
            location = location.value,
            date = date.value,
            startTime = startTime.value,
            targetTime = totalTime.value,
            restInterval = restInterval.value.toIntOrNull() ?: 0,
            restDuration = restDuration.value.toIntOrNull() ?: 0,
            agendas = agendaList,
            onSuccess = {
                isEditable.value = false
                originalTitle.value = title.value
                originalLocation.value = location.value
                originalDate.value = date.value
                originalStartTime.value = startTime.value
                originalEndTime.value = endTime.value
                originalTotalTime.value = totalTime.value
                originalRestInterval.value = restInterval.value
                originalRestDuration.value = restDuration.value
                originalAgendas.clear()
                originalAgendas.addAll(agendaList)
                originalParticipants.clear()
                originalParticipants.addAll(participants)
            },
            onError = {
                Log.e("MeetingEdit", "수정 실패: ${it.message}")
            }
        )
    }

    MeetingDetailScreen(
        navController = navController,
        id = safeMeeting.meetingId,
        title = title,
        location = location,
        participants = participants,
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
        onEditClicked = {
            if (isEditable.value && hasChanges()) {
                showCancelDialog.value = true
            } else {
                isEditable.value = !isEditable.value
            }
        },
        onConfirmEditClicked = { onConfirmEdit() }
    )

    if (showCancelDialog.value) {
        AlertDialog(
            onDismissRequest = { showCancelDialog.value = false },
            title = { Text("수정 취소") },
            text = { Text("수정한 내용이 모두 사라집니다. 정말 취소하시겠습니까?") },
            confirmButton = {
                TextButton(onClick = {
                    isEditable.value = false
                    showCancelDialog.value = false
                }) {
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog.value = false }) {
                    Text("취소")
                }
            }
        )
    }
}