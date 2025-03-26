package com.imhungry.jjongseol.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor() : ViewModel() {

    private val _loginResult = mutableStateOf<Result<String>?>(null)
    val loginResult: State<Result<String>?> = _loginResult

    fun onGoogleLoginResult(account: GoogleSignInAccount?) {
        viewModelScope.launch {
            account?.idToken?.let { token ->
                try {
                    val result = sendTokenToServer(token)
                    _loginResult.value = Result.success(result)
                } catch (e: Exception) {
                    _loginResult.value = Result.failure(e)
                }
            }
        }
    }

    private suspend fun sendTokenToServer(token: String): String {
        return "Success" // 응답값 리턴
    }

    fun clearLoginResult() {
        _loginResult.value = null
    }
}
