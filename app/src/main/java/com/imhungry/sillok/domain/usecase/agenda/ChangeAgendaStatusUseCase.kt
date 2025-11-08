package com.imhungry.sillok.domain.usecase.agenda

import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.repository.agenda.AgendaRepository
import javax.inject.Inject

class ChangeAgendaStatusUseCase @Inject constructor(
    private val repository: AgendaRepository
) {
    suspend operator fun invoke(
        meetingId: Long,
        agendaId: Long,
        isCompleted: Boolean
    ): ApiResult<Boolean> {
        return repository.changeAgendaStatus(meetingId, agendaId, isCompleted)
    }
}