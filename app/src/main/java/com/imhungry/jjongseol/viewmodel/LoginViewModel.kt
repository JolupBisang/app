package com.imhungry.jjongseol.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.imhungry.jjongseol.data.repository.AuthRepository
import com.imhungry.jjongseol.data.repository.LoginRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val loginRepository: LoginRepository
) : ViewModel() {

    val isLoggedIn: StateFlow<Boolean> = loginRepository.isLoggedInFlow

    fun saveToken(token: String) = loginRepository.saveToken(token)

    fun clearToken() = loginRepository.clearToken()

    fun launchGoogleLogin(context: Context) = authRepository.launchGoogleOAuth(context)
}

