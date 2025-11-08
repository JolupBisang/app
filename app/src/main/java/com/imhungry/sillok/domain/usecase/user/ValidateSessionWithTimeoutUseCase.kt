package com.imhungry.sillok.domain.usecase.user

import com.imhungry.sillok.data.local.TokenStore
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ValidateSessionWithTimeoutUseCase @Inject constructor(
    private val tokenStore: TokenStore
) {
    suspend operator fun invoke(): SessionValidationResult {
        // 토큰 확인
        val token = tokenStore.accessToken.first()
        return if (token.isNullOrBlank()) {
            SessionValidationResult.NoToken
        } else {
            SessionValidationResult.Valid
        }
    }
}

sealed class SessionValidationResult {
    object Valid : SessionValidationResult()
    object Invalid : SessionValidationResult()
    object NoToken : SessionValidationResult()
    object Timeout : SessionValidationResult()
}

