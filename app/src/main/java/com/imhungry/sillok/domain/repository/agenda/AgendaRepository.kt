package com.imhungry.sillok.domain.repository.agenda

import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.model.agenda.Agenda

interface AgendaRepository {
    suspend fun getAgendas(meetingId: Long): ApiResult<List<Agenda>>
    suspend fun addAgenda(meetingId: Long, content: List<String>): ApiResult<List<Long>>
    suspend fun updateAgenda(meetingId: Long, agendaId: Long, content: String): ApiResult<Long>
    suspend fun changeAgendaStatus(meetingId: Long, agendaId: Long, isCompleted: Boolean): ApiResult<Boolean>
    suspend fun deleteAgenda(meetingId: Long, agendaId: Long): ApiResult<Unit>
}
