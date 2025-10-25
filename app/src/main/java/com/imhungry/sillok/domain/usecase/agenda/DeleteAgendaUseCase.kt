package com.imhungry.sillok.domain.usecase.agenda

import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.repository.agenda.AgendaRepository
import javax.inject.Inject

class DeleteAgendaUseCase @Inject constructor(
    private val repository: AgendaRepository
) {
    suspend operator fun invoke(meetingId: Long, agendaId: Long): ApiResult<Unit> {
        return repository.deleteAgenda(meetingId, agendaId)
    }
}
