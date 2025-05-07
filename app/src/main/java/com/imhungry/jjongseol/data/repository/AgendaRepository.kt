package com.imhungry.jjongseol.data.repository

import android.util.Log
import com.imhungry.jjongseol.data.model.agenda.AgendaDto
import com.imhungry.jjongseol.data.network.AgendaApi
import com.imhungry.jjongseol.util.handleHttpException
import retrofit2.HttpException
import javax.inject.Inject

class AgendaRepository @Inject constructor(
    private val agendaApi: AgendaApi
) {
    suspend fun getAgendas(meetingId: Long): List<AgendaDto> {
        return try {
            agendaApi.getAgendas(meetingId).data.agendaDetails
        } catch (e: HttpException) {
            val error = handleHttpException(e)
            throw RuntimeException(error.message ?: "알 수 없는 오류 발생")
        }
    }
}

