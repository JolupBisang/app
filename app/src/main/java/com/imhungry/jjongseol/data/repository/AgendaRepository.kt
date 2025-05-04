package com.imhungry.jjongseol.data.repository

import android.util.Log
import com.imhungry.jjongseol.data.model.agenda.AgendaDto
import com.imhungry.jjongseol.data.network.AgendaApi
import javax.inject.Inject

class AgendaRepository @Inject constructor(
    private val agendaApi: AgendaApi
) {
    suspend fun getAgendas(meetingId: Long): List<AgendaDto> {
        val response = agendaApi.getAgendas(meetingId)
        Log.d("Agenda", "Api 응답: $response")
        return response.data.agendaDetails
    }
}

