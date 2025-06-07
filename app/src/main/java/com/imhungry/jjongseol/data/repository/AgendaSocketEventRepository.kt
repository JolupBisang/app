package com.imhungry.jjongseol.data.repository

import com.imhungry.jjongseol.data.model.agenda.dto.AgendaDto
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

object AgendaSocketEventRepository {
    private val _agendaUpdates = MutableSharedFlow<AgendaDto>()
    val agendaUpdates: SharedFlow<AgendaDto> = _agendaUpdates

    suspend fun emitAgendaUpdate(dto: AgendaDto) {
        _agendaUpdates.emit(dto)
    }
}