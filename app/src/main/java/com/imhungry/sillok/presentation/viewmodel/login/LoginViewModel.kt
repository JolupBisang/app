package com.imhungry.sillok.presentation.viewmodel.login

import android.content.Context
import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imhungry.sillok.BuildConfig
import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.usecase.user.LoginUseCase
import com.imhungry.sillok.presentation.state.login.LoginState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()
    
    companion object {
        private const val TAG = "LoginViewModel"
    }
    
    fun launchGoogleOAuth(context: Context) {
        val authUrl = ("https://accounts.google.com/o/oauth2/v2/auth/oauthchooseaccount" +
                "?client_id=${BuildConfig.OAUTH_CLIENT_ID}" +
                "&redirect_uri=${BuildConfig.OAUTH_REDIRECT_URI}" +
                "&response_type=code" +
                "&scope=email profile").toUri()

        val intent = CustomTabsIntent.Builder().build()
        intent.launchUrl(context, authUrl)
    }
    
    fun handleLogin(token: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            val result = loginUseCase(token)
            
            _state.value = when (result) {
                is ApiResult.Success -> {
                    _state.value.copy(
                        isLoading = false,
                        isLoginSuccess = true,
                        user = result.data
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
}