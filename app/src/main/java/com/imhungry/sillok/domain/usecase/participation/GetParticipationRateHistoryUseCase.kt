package com.imhungry.sillok.domain.usecase.participation

import com.imhungry.sillok.domain.repository.participation.ParticipationRateRepository
import javax.inject.Inject

class GetParticipationRateHistoryUseCase @Inject constructor(
    private val repository: ParticipationRateRepository
) {
    suspend operator fun invoke(meetingId: Long) =
        repository.getParticipationRateHistory(meetingId)
}
