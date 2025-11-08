package com.imhungry.sillok.data.mapper.participation

import com.imhungry.sillok.data.model.participation.UserParticipationRateDto
import com.imhungry.sillok.domain.model.participation.UserParticipationRate
import javax.inject.Inject

class ParticipationMapper @Inject constructor() {
    fun toDomain(dto: UserParticipationRateDto): UserParticipationRate {
        return UserParticipationRate(
            userId = dto.userId,
            rate = dto.rate ?: 0.0,
            totalParticipationChunk = dto.totalParticipationChunk ?: 0L
        )
    }
}