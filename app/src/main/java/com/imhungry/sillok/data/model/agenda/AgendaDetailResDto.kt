package com.imhungry.sillok.data.model.agenda

data class AgendaDetailResDto(
    val agendas: List<AgendaListInfoRes>
)

data class AgendaListInfoRes(
    val id: Long,
    val content: String,
    val isCompleted: Boolean
)