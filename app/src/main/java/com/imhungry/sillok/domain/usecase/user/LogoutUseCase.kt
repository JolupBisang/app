package com.imhungry.sillok.domain.usecase.user

import com.imhungry.sillok.data.local.TokenStore
import com.imhungry.sillok.data.local.UserStore
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val tokenStore: TokenStore,
    private val userStore: UserStore
) {
    suspend operator fun invoke() {
        tokenStore.clearTokens()
        userStore.clearUser()
    }
}

