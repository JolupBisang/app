package com.imhungry.jjongseol.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.imhungry.jjongseol.data.repository.AuthRepository
import com.imhungry.jjongseol.data.repository.LoginRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _isLoggedIn = MutableLiveData(loginRepository.isLoggedIn())
    val isLoggedIn: LiveData<Boolean> get() = _isLoggedIn

    /*fun saveToken(token: String) {
        loginRepository.saveToken(token)
        _isLoggedIn.value = true
    }*/

    fun saveToken(token: String) {
        loginRepository.saveToken(token)
        Log.d("LoginViewModel", "Token saved: $token")
        _isLoggedIn.value = true
        Log.d("LoginViewModel", "isLoggIn value: ${isLoggedIn.value}")
    }


    fun launchGoogleLogin(activityContext: Context) {
        authRepository.launchGoogleOAuth(activityContext)
    }
}

