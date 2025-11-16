package com.imhungry.sillok.data.repository.user

import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.model.user.User
import com.imhungry.sillok.domain.repository.user.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * debug 전용 UserRepository.
 * - 이메일로 검색 시 해당 이메일에 맞는 더미 사용자 반환
 * - 알려진 이메일이 아니면 기본 사용자 반환
 * - 회의 참가자 시뮬레이션에 유용
 */
@Singleton
class FakeUserRepository @Inject constructor() : UserRepository {

    companion object {
        // 더미 사용자 목록 (이메일 -> User 매핑)
        private val FAKE_USERS = mapOf(
            "joeungyeong23@gmail.com" to User(
                id = 1L,
                email = "joeungyeong23@gmail.com",
                nickname = "조은경",
                pictureURL = "https://avatars.githubusercontent.com/u/108224432?v=4"
            ),
            "hong@example.com" to User(
                id = 2L,
                email = "hong@example.com",
                nickname = "홍길동",
                pictureURL = "https://i.pravatar.cc/150?img=1"
            ),
            "kim@example.com" to User(
                id = 3L,
                email = "kim@example.com",
                nickname = "김영희",
                pictureURL = "https://i.pravatar.cc/150?img=2"
            ),
            "park@example.com" to User(
                id = 4L,
                email = "park@example.com",
                nickname = "박철수",
                pictureURL = "https://i.pravatar.cc/150?img=4"
            )
        )

        // 기본 사용자 (알려지지 않은 이메일용)
        private val DEFAULT_USER = User(
            id = 999L,
            email = "unknown@example.com",
            nickname = "알 수 없는 사용자",
            pictureURL = ""
        )
    }

    override suspend fun getUserInfo(email: String): ApiResult<User> {
        // 알려진 이메일이면 해당 사용자 반환, 아니면 기본 사용자 반환
        val user = FAKE_USERS[email.lowercase()] ?: run {
            // 알려지지 않은 이메일이면 이메일 기반으로 동적 생성
            val emailHash = email.hashCode().toLong().let { if (it < 0) -it else it }
            DEFAULT_USER.copy(
                id = emailHash % 1000 + 100, // 100-1099 범위의 ID
                email = email,
                nickname = "사용자 ${email.split("@").firstOrNull() ?: "알 수 없음"}"
            )
        }
        return ApiResult.Success(user)
    }

    override suspend fun getMyProfile(): ApiResult<User> {
        // 현재 사용자 정보 반환 (getUserInfo와 동일한 사용자)
        val myUser = FAKE_USERS["joeungyeong23@gmail.com"] ?: DEFAULT_USER
        return ApiResult.Success(myUser)
    }
}
