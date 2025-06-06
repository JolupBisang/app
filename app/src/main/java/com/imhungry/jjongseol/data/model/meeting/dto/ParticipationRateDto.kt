package com.imhungry.jjongseol.data.model.meeting.dto

data class ParticipationRateDto(
    val userId: Long,
    val nickname: String,
    val rate: Double
)