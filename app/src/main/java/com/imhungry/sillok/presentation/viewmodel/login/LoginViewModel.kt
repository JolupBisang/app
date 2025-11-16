package com.imhungry.sillok.presentation.viewmodel.login

import android.content.Context
import android.util.Log
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

/**
 * 로그인 화면의 UI 상태와 비즈니스 로직을 관리하는 ViewModel
 *
 * 주요 기능:
 * - Google OAuth 로그인 시작
 * - 딥링크로 받은 토큰을 통한 로그인 처리
 * - 로그인 성공/실패 상태 관리
 * - 음성 인식 완료 여부 확인
 */
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
        Log.d(TAG, "[launchGoogleOAuth] Google OAuth 로그인 시작")
        launchGoogleOAuthUseCase(context)
    }

    fun handleLogin(token: String) {
        Log.d(TAG, "[handleLogin] 로그인 처리 시작")
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            Log.d(TAG, "[handleLogin] 로딩 상태로 변경")

            val result = loginUseCase(token)
            Log.d(TAG, "[handleLogin] LoginUseCase 결과: ${result::class.simpleName}")

            _state.value = when (result) {
                is ApiResult.Success -> {
                    Log.d(TAG, "[handleLogin] 로그인 성공: userId=${result.data.id}, email=${result.data.email}")
                    // 로그인 성공 시 음성 인식 완료 여부 확인
                    val isVoiceRecognitionCompleted = checkVoiceRecognitionCompletionUseCase()
                    Log.d(TAG, "[handleLogin] 음성 인식 완료 여부: $isVoiceRecognitionCompleted")
                    _state.value.copy(
                        isLoading = false,
                        isLoginSuccess = true,
                        isVoiceRecognitionCompleted = isVoiceRecognitionCompleted
                    )
                }

                is ApiResult.Failure -> {
                    Log.e(TAG, "[handleLogin] 로그인 실패: ${result.message}")
                    // 로그인 실패 시 에러 메시지 표시
                    _state.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
            }
            Log.d(TAG, "[handleLogin] 로그인 처리 완료")
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

