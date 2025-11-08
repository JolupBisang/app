package com.imhungry.sillok.domain.model.participation

data class UserParticipationRate(
    val userId: Long,
    val rate: Double,
    val totalParticipationChunk: Long
)
