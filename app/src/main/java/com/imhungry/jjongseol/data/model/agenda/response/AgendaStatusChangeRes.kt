package com.imhungry.jjongseol.data.model.agenda.response

data class AgendaStatusChangeRes(
    val meetingId: Long?,
    val agendaId: Long?,
    val isCompleted: Boolean?
)