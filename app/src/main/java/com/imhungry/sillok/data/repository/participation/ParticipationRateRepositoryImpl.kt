package com.imhungry.sillok.data.repository.participation

import com.imhungry.sillok.data.mapper.participation.ParticipationMapper
import com.imhungry.sillok.data.remote.participation.ParticipationRateApi
import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.model.participation.UserParticipationRate
import com.imhungry.sillok.domain.repository.participation.ParticipationRateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ParticipationRateRepositoryImpl @Inject constructor(
    private val api: ParticipationRateApi,
    private val mapper: ParticipationMapper
) : ParticipationRateRepository {

    override suspend fun getParticipationRateHistory(
        meetingId: Long
    ): ApiResult<List<UserParticipationRate>> = withContext(Dispatchers.IO) {
        try {
            val res = api.getParticipationRateHistory(meetingId)
            if (res.isSuccessful) {
                val dto = res.body()
                if (dto != null) {
                    val users = dto.userParticipationRates.map { mapper.toDomain(it) }
                    ApiResult.Success(users)
                } else {
                    ApiResult.Failure("응답 파싱 오류")
                }
            } else {
                ApiResult.Failure(res.message())
            }
        } catch (e: Exception) {
            ApiResult.Failure(e.localizedMessage ?: "알 수 없는 오류")
        }
    }
}
