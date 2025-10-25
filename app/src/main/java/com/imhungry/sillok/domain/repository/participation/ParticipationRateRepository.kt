package com.imhungry.sillok.domain.repository.participation

import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.model.participation.UserParticipationRate

interface ParticipationRateRepository {
    suspend fun getParticipationRateHistory(
        meetingId: Long
    ): ApiResult<List<UserParticipationRate>>
}
