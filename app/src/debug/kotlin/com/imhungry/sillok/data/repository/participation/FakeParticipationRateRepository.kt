package com.imhungry.sillok.data.repository.participation

import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.model.participation.UserParticipationRate
import com.imhungry.sillok.domain.repository.participation.ParticipationRateRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeParticipationRateRepository @Inject constructor() : ParticipationRateRepository {

    companion object {
        private val DUMMY_PARTICIPATION_RATES = mapOf(
            1L to 0.25,
            2L to 0.20,
            3L to 0.30,
            4L to 0.25,
        )
    }

    override suspend fun getParticipationRateHistory(
        meetingId: Long
    ): ApiResult<List<UserParticipationRate>> {
        val participationRates = DUMMY_PARTICIPATION_RATES.map { (userId, rate) ->
            val nickname = when (userId) {
                1L -> "조은경"
                2L -> "홍길동"
                3L -> "김영희"
                4L -> "박철수"
                else -> "사용자 $userId"
            }
            
            UserParticipationRate(
                userId = userId,
                nickname = nickname,
                rate = rate
            )
        }.sortedByDescending { it.rate } // 참여율 높은 순으로 정렬

        return ApiResult.Success(participationRates)
    }
}
