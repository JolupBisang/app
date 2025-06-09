package com.imhungry.jjongseol.data.repository

import com.imhungry.jjongseol.data.model.participationrate.response.ParticipationRateHistoryRes
import com.imhungry.jjongseol.data.network.api.ParticipationRateApi
import javax.inject.Inject

sealed class ParticipationRateResult<out T> {
    data class Success<T>(val data: T) : ParticipationRateResult<T>()
    data class Error(val message: String) : ParticipationRateResult<Nothing>()
    data class Exception(val throwable: Throwable) : ParticipationRateResult<Nothing>()
}

class ParticipationRateRepository @Inject constructor(
    private val api: ParticipationRateApi
) {
    suspend fun getParticipationRateHistory(meetingId: Long): ParticipationRateResult<ParticipationRateHistoryRes> {
        return try {
            val response = api.getParticipationRateHistory(meetingId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) ParticipationRateResult.Success(body)
                else ParticipationRateResult.Error("응답이 비어있음")
            } else {
                ParticipationRateResult.Error("서버 오류: ${response.code()}")
            }
        } catch (e: Exception) {
            ParticipationRateResult.Exception(e)
        }
    }
}
