package com.imhungry.sillok.presentation.viewmodel.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.usecase.user.LaunchGoogleOAuthUseCase
import com.imhungry.sillok.domain.usecase.user.LoginUseCase
import com.imhungry.sillok.domain.usecase.voice.CheckVoiceRecognitionCompletionUseCase
import com.imhungry.sillok.presentation.state.login.LoginState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val launchGoogleOAuthUseCase: LaunchGoogleOAuthUseCase,
    private val loginUseCase: LoginUseCase,
    private val checkVoiceRecognitionCompletionUseCase: CheckVoiceRecognitionCompletionUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    companion object {
        private const val TAG = "LoginViewModel"
    }

    fun launchGoogleOAuth(context: Context) {
        launchGoogleOAuthUseCase(context)
    }

    fun handleLogin(token: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            val result = loginUseCase(token)

            _state.value = when (result) {
                is ApiResult.Success -> {
                    val isVoiceRecognitionCompleted = checkVoiceRecognitionCompletionUseCase()
                    _state.value.copy(
                        isLoading = false,
                        isLoginSuccess = true,
                        isVoiceRecognitionCompleted = isVoiceRecognitionCompleted
                    )
                }

                is ApiResult.Failure -> {
                    _state.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun resetLoginSuccess() {
        _state.value = _state.value.copy(isLoginSuccess = false)
    }

    fun showExitDialog() {
        _state.value = _state.value.copy(showExitDialog = true)
    }

    fun dismissExitDialog() {
        _state.value = _state.value.copy(showExitDialog = false)
    }
}

