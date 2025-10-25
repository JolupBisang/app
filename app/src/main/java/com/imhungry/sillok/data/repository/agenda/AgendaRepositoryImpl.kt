package com.imhungry.sillok.data.repository.agenda

import com.imhungry.sillok.data.mapper.agenda.AgendaMapper
import com.imhungry.sillok.data.model.agenda.AgendaCreateReqDto
import com.imhungry.sillok.data.model.agenda.AgendaStatusReqDto
import com.imhungry.sillok.data.model.agenda.AgendaUpdateReqDto
import com.imhungry.sillok.data.remote.agenda.AgendaApi
import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.model.agenda.Agenda
import com.imhungry.sillok.domain.repository.agenda.AgendaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AgendaRepositoryImpl @Inject constructor(
    private val api: AgendaApi,
    private val mapper: AgendaMapper
) : AgendaRepository {

    override suspend fun getAgendas(meetingId: Long): ApiResult<List<Agenda>> = withContext(Dispatchers.IO) {
        try {
            val res = api.getAgendas(meetingId)
            if (res.isSuccessful) {
                val agendas = res.body()?.agendas?.map { mapper.toDomain(it) } ?: emptyList()
                ApiResult.Success(agendas)
            } else {
                ApiResult.Failure(res.message())
            }
        } catch (e: Exception) {
            ApiResult.Failure(e.localizedMessage ?: "알 수 없는 오류")
        }
    }

    override suspend fun addAgenda(meetingId: Long, content: String): ApiResult<Long> = withContext(Dispatchers.IO) {
        try {
            val res = api.addAgenda(meetingId, AgendaCreateReqDto(content))
            if (res.isSuccessful) {
                val created = res.body()?.agendaDetails?.firstOrNull()?.agendaId
                if (created != null) ApiResult.Success(created) else ApiResult.Failure("응답 파싱 오류")
            } else {
                ApiResult.Failure(res.message())
            }
        } catch (e: Exception) {
            ApiResult.Failure(e.localizedMessage ?: "알 수 없는 오류")
        }
    }

    override suspend fun updateAgenda(meetingId: Long, agendaId: Long, content: String): ApiResult<Long> = withContext(Dispatchers.IO) {
        try {
            val res = api.updateAgenda(meetingId = meetingId, agendaId = agendaId, request = AgendaUpdateReqDto(content))
            if (res.isSuccessful) {
                val updatedId = res.body()?.agendaId
                if (updatedId != null) ApiResult.Success(updatedId) else ApiResult.Failure("응답 파싱 오류")
            } else {
                ApiResult.Failure(res.message())
            }
        } catch (e: Exception) {
            ApiResult.Failure(e.localizedMessage ?: "알 수 없는 오류")
        }
    }

    override suspend fun changeAgendaStatus(meetingId: Long, agendaId: Long, isCompleted: Boolean): ApiResult<Boolean> = withContext(Dispatchers.IO) {
        try {
            val res = api.changeAgendaStatus(meetingId = meetingId, agendaId = agendaId, request = AgendaStatusReqDto(isCompleted))
            if (res.isSuccessful) {
                val status = res.body()?.isCompleted
                if (status != null) ApiResult.Success(status) else ApiResult.Failure("응답 파싱 오류")
            } else {
                ApiResult.Failure(res.message())
            }
        } catch (e: Exception) {
            ApiResult.Failure(e.localizedMessage ?: "알 수 없는 오류")
        }
    }

    override suspend fun deleteAgenda(meetingId: Long, agendaId: Long): ApiResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val res = api.deleteAgenda(meetingId = meetingId, agendaId = agendaId)
            if (res.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Failure(res.message())
            }
        } catch (e: Exception) {
            ApiResult.Failure(e.localizedMessage ?: "알 수 없는 오류")
        }
    }
}
