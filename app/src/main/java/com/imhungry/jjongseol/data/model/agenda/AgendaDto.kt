package com.imhungry.jjongseol.data.model.agenda

data class AgendaDto(
    val agendaId: Long,
    val content: String,
    val isCompleted: Boolean
)

data class AgendaListResponse(
    val agendaDetails: List<AgendaDto>
)
