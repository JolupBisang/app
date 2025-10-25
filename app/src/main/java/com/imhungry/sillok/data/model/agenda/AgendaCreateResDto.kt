package com.imhungry.sillok.data.model.agenda

data class AgendaCreateResDto(
    val meetingId: Long,
    val agendaDetails: List<AgendaDetailInfoRes>
)

data class AgendaDetailInfoRes(
    val agendaId: Long?,
    val content: String?
)