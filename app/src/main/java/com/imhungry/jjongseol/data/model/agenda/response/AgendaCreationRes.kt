package com.imhungry.jjongseol.data.model.agenda.response


data class AgendaCreationRes(
    val meetingId: Long?,
    val agendaDetails: List<AgendaDetailInfoRes>?
)

data class AgendaDetailInfoRes(
    val agendaId: Long?,
    val content: String?
)