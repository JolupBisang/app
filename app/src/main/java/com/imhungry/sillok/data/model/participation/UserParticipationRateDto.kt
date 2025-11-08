package com.imhungry.sillok.data.model.participation

data class UserParticipationRateDto(
    val userId: Long,
    val rate: Double,
    val totalParticipationChunk: Long
)
