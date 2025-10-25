package com.imhungry.sillok.presentation.viewmodel.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import android.util.Base64
import org.json.JSONObject
import java.util.Date
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.TimeZone
import com.imhungry.sillok.data.local.VoiceRecognitionStore
import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.usecase.user.ValidateSessionUseCase
import com.imhungry.sillok.presentation.state.splash.SplashState
import com.imhungry.sillok.presentation.state.voice.VoiceRecognitionConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val validateSessionUseCase: ValidateSessionUseCase,
    private val voiceRecognitionStore: VoiceRecognitionStore
) : ViewModel() {
    
    private val _state = MutableStateFlow(SplashState())
    val state: StateFlow<SplashState> = _state.asStateFlow()
    
    init {
        validateSession()
    }

    private fun validateSession() {
        viewModelScope.launch {
            delay(1000L)

            val isValid = validateSessionUseCase()

            if (isValid) {
                val voiceState = voiceRecognitionStore.voiceRecognitionState.first()
                if (!voiceState.isCompleted && voiceState.currentStep > VoiceRecognitionConstants.TOTAL_STEPS) {
                    launch { voiceRecognitionStore.setCompleted(true) }
                }

                _state.value = _state.value.copy(
                    isLoading = false,
                    isSessionValidated = true,
                    isLoggedIn = true,
                    isVoiceRecognitionCompleted = voiceState.isCompleted
                )
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    isSessionValidated = true,
                    isLoggedIn = false,
                    isVoiceRecognitionCompleted = false
                )
            }
        }
    }

    // JWT의 exp(초)를 파싱하여 한국 시간 문자열로 변환
    private fun decodeJwtExpToKst(token: String): String? {
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return null
            val payloadBytes = Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
            val payload = JSONObject(String(payloadBytes))
            val expSeconds = payload.optLong("exp", -1L)
            if (expSeconds <= 0L) return null
            val date = Date(expSeconds * 1000)
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
            sdf.timeZone = TimeZone.getTimeZone("Asia/Seoul")
            sdf.format(date)
        } catch (e: Exception) {
            null
        }
    }
    
    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
} 