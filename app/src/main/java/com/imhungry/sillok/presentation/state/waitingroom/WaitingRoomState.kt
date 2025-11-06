package com.imhungry.sillok.presentation.state.waitingroom

import com.imhungry.sillok.domain.model.agenda.Agenda

data class WaitingRoomState(
    val meetingId: Long = 1L,
    val agendas: List<Agenda> = emptyList(),
    val targetTimeDisplay: String = "00:00:00",
    val isLoading: Boolean = false,
    val error: String? = null
)