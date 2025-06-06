package com.imhungry.jjongseol.data.repository

import com.imhungry.jjongseol.data.model.meeting.dto.ParticipationRateDto
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Singleton

@Singleton
object ParticipationRateRepository {
    private val _participationRates = MutableSharedFlow<ParticipationRateDto>(replay = 1)
    val participationRates = _participationRates.asSharedFlow()

    suspend fun emitParticipationRate(dto: ParticipationRateDto) {
        _participationRates.emit(dto)
    }
}