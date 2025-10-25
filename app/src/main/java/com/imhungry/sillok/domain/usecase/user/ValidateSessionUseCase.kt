package com.imhungry.sillok.domain.usecase.user

import com.imhungry.sillok.data.local.TokenStore
import com.imhungry.sillok.data.local.UserStore
import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.repository.user.UserRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ValidateSessionUseCase @Inject constructor(
    private val tokenStore: TokenStore,
    private val userStore: UserStore,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Boolean {
        val token = tokenStore.accessToken.first()
        if (token.isNullOrBlank()) return false

        return when (val res = userRepository.getMyProfile()) {
            is ApiResult.Success -> {
                userStore.saveUser(res.data)
                true
            }
            is ApiResult.Failure -> {
                tokenStore.clearTokens()
                userStore.clearUser()
                false
            }
        }
    }
}


