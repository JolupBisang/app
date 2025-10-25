package com.imhungry.sillok.domain.usecase.user

import com.imhungry.sillok.data.local.TokenStore
import com.imhungry.sillok.data.local.UserStore
import com.imhungry.sillok.domain.model.user.User
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class CheckLoginStatusUseCase @Inject constructor(
    private val tokenStore: TokenStore,
    private val userStore: UserStore
) {
    operator fun invoke() = combine(
        tokenStore.accessToken,
        userStore.user
    ) { token, user ->
        LoginStatus(
            isLoggedIn = token != null && user != null,
            token = token,
            user = user
        )
    }
}

data class LoginStatus(
    val isLoggedIn: Boolean,
    val token: String?,
    val user: User?
)

