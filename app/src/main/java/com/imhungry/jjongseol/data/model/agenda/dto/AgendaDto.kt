package com.imhungry.jjongseol.data.model.agenda.dto

data class AgendaDto(
    val agendaId: Long,
    val content: String,
    val isCompleted: Boolean
)