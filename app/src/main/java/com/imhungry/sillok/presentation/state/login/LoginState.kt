package com.imhungry.sillok.presentation.state.login

import com.imhungry.sillok.domain.model.user.User

data class LoginState(
    val isLoading: Boolean = false,
    val isLoginSuccess: Boolean = false,
    val user: User? = null,
    val error: String? = null
)