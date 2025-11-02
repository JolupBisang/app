package com.imhungry.sillok.presentation.state.login

data class LoginState(
    val isLoading: Boolean = false,
    val isLoginSuccess: Boolean = false,
    val isVoiceRecognitionCompleted: Boolean = false,
    val error: String? = null
)