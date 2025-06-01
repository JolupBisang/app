package com.imhungry.jjongseol.data.repository

import android.util.Log
import com.imhungry.jjongseol.data.model.agenda.AgendaDto
import com.imhungry.jjongseol.data.network.api.AgendaApi
import com.imhungry.jjongseol.data.network.client.handleHttpException
import retrofit2.HttpException
import javax.inject.Inject

class AgendaRepository @Inject constructor(
    private val agendaApi: AgendaApi
) {
    suspend fun getAgendas(meetingId: Long): List<AgendaDto> {
        return try {
            val response = agendaApi.getAgendas(meetingId)
            if (response.isSuccessful) {
                val body = response.body()
                body?.data?.agendaDetails ?: emptyList()
            } else {
                Log.e("AGENDA_REPOSITORY", "HTTP 실패: ${response.code()} ${response.message()}")
                throw RuntimeException("아젠다 로드 실패 (code=${response.code()})")
            }
        } catch (e: HttpException) {
            val error = handleHttpException(e)
            throw RuntimeException(error.message ?: "알 수 없는 오류 발생")
        }
    }
}

