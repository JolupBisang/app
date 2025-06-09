package com.imhungry.jjongseol.data.repository.event

import com.imhungry.jjongseol.data.model.participationrate.dto.ParticipationRateDto
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Singleton

@Singleton
object ParticipationRateEventRepository {
    private val _participationRates = MutableSharedFlow<ParticipationRateDto>(replay = 1)
    val participationRates = _participationRates.asSharedFlow()

    suspend fun emitParticipationRate(dto: ParticipationRateDto) {
        _participationRates.emit(dto)
    }
}