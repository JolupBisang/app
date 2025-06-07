package com.imhungry.jjongseol.data.repository

import com.imhungry.jjongseol.data.model.agenda.request.AgendaStatusReq
import com.imhungry.jjongseol.data.model.agenda.response.AgendaChangeStatusRes
import com.imhungry.jjongseol.data.model.agenda.response.AgendaDetailRes
import com.imhungry.jjongseol.data.model.response.ErrorResponse
import com.imhungry.jjongseol.data.network.api.AgendaApi
import com.google.gson.Gson
import com.imhungry.jjongseol.data.model.agenda.dto.AgendaDto
import com.imhungry.jjongseol.data.model.agenda.request.AgendaCreateReq
import com.imhungry.jjongseol.data.model.agenda.request.AgendaUpdateReq
import com.imhungry.jjongseol.data.model.response.SuccessResponse
import retrofit2.Response
import javax.inject.Inject

sealed class AgendaResult<out T> {
    data class Success<T>(val data: T) : AgendaResult<T>()
    data class Error(val message: String, val errorResponse: ErrorResponse? = null) : AgendaResult<Nothing>()
    data class Exception(val throwable: Throwable) : AgendaResult<Nothing>()
}

class AgendaRepository @Inject constructor(
    private val agendaApi: AgendaApi
) {
    suspend fun getAgendas(meetingId: Long): AgendaResult<AgendaDetailRes> {
        return try {
            val response = agendaApi.getAgendas(meetingId)
            handleApiResponse(response)
        } catch (e: Exception) {
            AgendaResult.Exception(e)
        }
    }

    suspend fun changeAgendaStatus(agendaId: Long, isCompleted: Boolean): AgendaResult<AgendaChangeStatusRes> {
        return try {
            val response = agendaApi.changeAgendaStatus(agendaId, AgendaStatusReq(isCompleted))
            handleApiResponse(response)
        } catch (e: Exception) {
            AgendaResult.Exception(e)
        }
    }

    private inline fun <reified T> handleApiResponse(response: Response<SuccessResponse<T>>): AgendaResult<T> {
        return if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                if (T::class == Unit::class) {
                    @Suppress("UNCHECKED_CAST")
                    AgendaResult.Success(Unit as T)
                } else if (body.data != null) {
                    AgendaResult.Success(body.data)
                } else {
                    AgendaResult.Error("서버 응답이 올바르지 않습니다.", null)
                }
            } else {
                AgendaResult.Error("서버 응답이 없습니다.", null)
            }
        } else {
            val errorBody = response.errorBody()?.string()
            val errorResponse = try {
                if (errorBody != null) Gson().fromJson(errorBody, ErrorResponse::class.java) else null
            } catch (e: Exception) {
                null
            }
            AgendaResult.Error(errorResponse?.message ?: "서버 오류 발생", errorResponse)
        }
    }
    suspend fun addAgenda(meetingId: Long, content: String): AgendaResult<AgendaDto> {
        return try {
            val response = agendaApi.addAgenda(meetingId, AgendaCreateReq(content))
            handleApiResponse(response)
        } catch (e: Exception) {
            AgendaResult.Exception(e)
        }
    }

    suspend fun deleteAgenda(agendaId: Long): AgendaResult<Unit> {
        return try {
            val response = agendaApi.deleteAgenda(agendaId)
            handleApiResponse(response)
        } catch (e: Exception) {
            AgendaResult.Exception(e)
        }
    }

    suspend fun updateAgenda(agendaId: Long, content: String): AgendaResult<Long> {
        return try {
            val response = agendaApi.updateAgenda(agendaId, AgendaUpdateReq(content))
            handleApiResponse(response)
        } catch (e: Exception) {
            AgendaResult.Exception(e)
        }
    }

}
