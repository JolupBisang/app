package com.imhungry.sillok.domain.usecase.user

import com.imhungry.sillok.data.local.TokenStore
import com.imhungry.sillok.data.local.UserStore
import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.repository.user.UserRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

class ValidateSessionWithTimeoutUseCase @Inject constructor(
    private val tokenStore: TokenStore,
    private val userStore: UserStore,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): SessionValidationResult {
        // 토큰 확인
        val token = tokenStore.accessToken.first()
        if (token.isNullOrBlank()) {
            return SessionValidationResult.NoToken
        }

        // 2초 타임아웃으로 사용자 정보 조회
        return try {
            val result = withTimeout(2000L) {
                userRepository.getMyProfile()
            }

            when (result) {
                is ApiResult.Success -> {
                    userStore.saveUser(result.data)
                    SessionValidationResult.Valid
                }
                is ApiResult.Failure -> {
                    tokenStore.clearTokens()
                    userStore.clearUser()
                    SessionValidationResult.Invalid
                }
            }
        } catch (e: Exception) {
            // 타임아웃 또는 예외 발생
            tokenStore.clearTokens()
            userStore.clearUser()
            SessionValidationResult.Timeout
        }
    }
}

sealed class SessionValidationResult {
    object Valid : SessionValidationResult()
    object Invalid : SessionValidationResult()
    object NoToken : SessionValidationResult()
    object Timeout : SessionValidationResult()
}

