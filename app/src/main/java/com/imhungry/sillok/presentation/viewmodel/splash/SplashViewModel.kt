package com.imhungry.sillok.presentation.viewmodel.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imhungry.sillok.domain.usecase.voice.CheckVoiceRecognitionCompletionUseCase
import com.imhungry.sillok.domain.usecase.user.SessionValidationResult
import com.imhungry.sillok.domain.usecase.user.ValidateSessionWithTimeoutUseCase
import com.imhungry.sillok.presentation.state.splash.SplashState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val validateSessionWithTimeoutUseCase: ValidateSessionWithTimeoutUseCase,
    private val checkVoiceRecognitionCompletionUseCase: CheckVoiceRecognitionCompletionUseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow(SplashState())
    val state: StateFlow<SplashState> = _state.asStateFlow()
    
    init {
        validateSession()
    }

    private fun validateSession() {
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            
            val validationResult = validateSessionWithTimeoutUseCase()
            
            when (validationResult) {
                is SessionValidationResult.Valid -> {
                    val isVoiceRecognitionCompleted = checkVoiceRecognitionCompletionUseCase()
                    
                    ensureMinimumSplashTime(startTime)
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        isVoiceRecognitionCompleted = isVoiceRecognitionCompleted
                    )
                }
                is SessionValidationResult.Invalid,
                is SessionValidationResult.NoToken,
                is SessionValidationResult.Timeout -> {
                    ensureMinimumSplashTime(startTime)
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isLoggedIn = false
                    )
                }
            }
        }
    }
    
    private suspend fun ensureMinimumSplashTime(startTime: Long) {
        val elapsed = System.currentTimeMillis() - startTime
        val remaining = 1000L - elapsed
        if (remaining > 0) {
            delay(remaining)
        }
    }
} 