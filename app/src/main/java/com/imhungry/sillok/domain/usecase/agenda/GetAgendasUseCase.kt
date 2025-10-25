package com.imhungry.sillok.domain.usecase.agenda

import com.imhungry.sillok.domain.repository.agenda.AgendaRepository
import javax.inject.Inject

class GetAgendasUseCase @Inject constructor(
    private val repository: AgendaRepository
) {
    suspend operator fun invoke(meetingId: Long) = repository.getAgendas(meetingId)
}
