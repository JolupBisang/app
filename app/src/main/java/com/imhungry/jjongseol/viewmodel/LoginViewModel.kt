package com.imhungry.jjongseol.viewmodel

import android.content.Context
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

    val isLoggedIn: LiveData<Boolean> = MutableLiveData(loginRepository.isLoggedIn())

    fun saveToken(token: String) {
        loginRepository.saveToken(token)
        (isLoggedIn as MutableLiveData).value = true
    }

    fun launchGoogleLogin(activityContext: Context) {
        authRepository.launchGoogleOAuth(activityContext)
    }
}

