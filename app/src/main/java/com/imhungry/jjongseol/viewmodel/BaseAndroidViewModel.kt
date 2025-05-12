package com.imhungry.jjongseol.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.imhungry.jjongseol.data.model.error.ApiError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

open class BaseAndroidViewModel(application: Application) : AndroidViewModel(application) {
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    open fun setError(apiError: ApiError) {
        _errorMessage.value = when (apiError.message) {
            "만료된 토큰입니다." -> "TOKEN_EXPIRED"
            else -> apiError.message ?: "알 수 없는 오류 발생"
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}
