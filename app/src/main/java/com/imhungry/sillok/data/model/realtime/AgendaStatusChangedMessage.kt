package com.imhungry.sillok.data.model.realtime

data class AgendaStatusChangedMessage(
    val agendaId: Long,
    val isCompleted: Boolean
)