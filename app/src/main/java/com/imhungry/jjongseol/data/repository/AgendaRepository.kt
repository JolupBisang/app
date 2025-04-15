package com.imhungry.jjongseol.data.repository

import com.imhungry.jjongseol.data.model.AgendaDto
import com.imhungry.jjongseol.data.network.AgendaApi
import javax.inject.Inject

class AgendaRepository @Inject constructor(
    private val agendaApi: AgendaApi
) {
    suspend fun getAgendas(meetingId: Long): List<AgendaDto> {
        return agendaApi.getAgendas(meetingId).agendaDetails
    }
}

