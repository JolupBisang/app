package com.imhungry.sillok.data.mapper.agenda

import com.imhungry.sillok.data.model.agenda.AgendaListInfoRes
import com.imhungry.sillok.domain.model.agenda.Agenda
import javax.inject.Inject

class AgendaMapper @Inject constructor() {
    fun toDomain(dto: AgendaListInfoRes): Agenda {
        return Agenda(
            agendaId = dto.id,
            content = dto.content,
            isCompleted = dto.isCompleted
        )
    }
}