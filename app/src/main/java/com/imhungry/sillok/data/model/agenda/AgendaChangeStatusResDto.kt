package com.imhungry.sillok.data.model.agenda

data class AgendaChangeStatusResDto(
    val meetingId: Long,
    val agendaId: Long,
    val isCompleted: Boolean
)
