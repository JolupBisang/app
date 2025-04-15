package com.imhungry.jjongseol.data.model

data class AgendaDto(
    val agendaId: Long,
    val content: String,
    val isCompleted: Boolean
)

data class AgendaListResponse(
    val agendaDetails: List<AgendaDto>
)
