package com.imhungry.jjongseol.data.model.participationrate.response

data class ParticipationRateHistoryRes(
    val userParticipationRates: List<UserParticipationRate>
) {
    data class UserParticipationRate(
        val userId: Long,
        val nickname: String,
        val rate: Double
    )
}