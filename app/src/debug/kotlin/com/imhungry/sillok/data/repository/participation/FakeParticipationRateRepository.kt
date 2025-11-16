package com.imhungry.sillok.data.repository.participation

import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.model.participation.UserParticipationRate
import com.imhungry.sillok.domain.repository.participation.ParticipationRateRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * debug 전용 ParticipationRateRepository.
 *
 * 회의 참가자들의 참여율을 시뮬레이션합니다.
 * FakeUserRepository의 사용자 ID와 매칭하여 참여율을 제공합니다.
 */
@Singleton
class FakeParticipationRateRepository @Inject constructor() : ParticipationRateRepository {

    companion object {
        // 더미 참여율 데이터 (userId -> rate)
        // FakeUserRepository의 사용자 ID와 매칭:
        // 1L: 조은경, 2L: 홍길동, 3L: 김영희, 4L: 박철수
        // rate는 0.0 ~ 1.0 사이의 값 (참여율 비율)
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
        // 더미 참여율 데이터를 UserParticipationRate 리스트로 변환
        val participationRates = DUMMY_PARTICIPATION_RATES.map { (userId, rate) ->
            // FakeUserRepository의 닉네임 매핑
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
