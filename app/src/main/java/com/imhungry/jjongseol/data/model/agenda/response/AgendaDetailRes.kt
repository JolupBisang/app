package com.imhungry.jjongseol.data.model.agenda.response

data class AgendaDetailRes(
    val agendaDetails: List<AgendaDetailDto>
)

data class AgendaDetailDto(
    val agendaId: Long,
    val content: String,
    val isCompleted: Boolean
)