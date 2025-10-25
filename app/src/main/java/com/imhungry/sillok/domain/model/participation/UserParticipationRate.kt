package com.imhungry.sillok.domain.model.participation

data class UserParticipationRate(
    val userId: Long,
    val nickname: String,
    val rate: Double
)
