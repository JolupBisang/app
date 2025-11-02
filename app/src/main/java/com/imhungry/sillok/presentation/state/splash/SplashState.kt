package com.imhungry.sillok.presentation.state.splash

import androidx.compose.runtime.Stable

@Stable
data class SplashState(
    val isLoading: Boolean = true,
    val isLoggedIn: Boolean = false,
    val isVoiceRecognitionCompleted: Boolean = false
) 