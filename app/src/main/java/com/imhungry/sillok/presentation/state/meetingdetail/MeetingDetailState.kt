package com.imhungry.sillok.presentation.state.meetingdetail

data class MeetingDetailState(
    val meetingId: Long = 1L,
    val title: String = "",
    val hostEmail: String = "",
    val participantEmails: List<String> = emptyList(),
    val date: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val targetTime: String = "",
    val location: String = "",
    val agendas: List<String> = emptyList(),
    val breakInterval: String = "",
    val breakDuration: String = "",
    val isHost: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)
