package com.imhungry.jjongseol.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.imhungry.jjongseol.data.repository.AuthRepository
import com.imhungry.jjongseol.data.repository.LoginRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val loginRepository: LoginRepository
) : ViewModel() {

    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess: StateFlow<Boolean> = _loginSuccess

    fun onLoginSuccess(token: String) {
        saveToken(token)
        _loginSuccess.value = true
    }

    fun saveToken(token: String) = loginRepository.saveToken(token)

    fun clearToken() = loginRepository.clearToken()

    fun getToken() = loginRepository.getToken()

    fun isLoggedIn(): Boolean = loginRepository.isLoggedIn()

    fun launchGoogleLogin(context: Context) = authRepository.launchGoogleOAuth(context)
}

