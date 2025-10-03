package com.imhungry.jjongseol.data.model.agenda.response

data class AgendaListRes(
    val agendas: List<AgendaListInfoRes>?
)

data class AgendaListInfoRes(
    val id: Long?,
    val content: String?,
    val isCompleted: Boolean?
)