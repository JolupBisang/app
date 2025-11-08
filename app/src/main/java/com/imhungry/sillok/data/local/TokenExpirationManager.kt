package com.imhungry.sillok.data.local

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenExpirationManager @Inject constructor() {

    private val _shouldNavigateToLogin = MutableSharedFlow<Boolean>()
    val shouldNavigateToLogin: SharedFlow<Boolean> = _shouldNavigateToLogin

    suspend fun notifyTokenExpired() {
        _shouldNavigateToLogin.emit(true)
    }

    suspend fun clearNavigationEvent() {
        _shouldNavigateToLogin.emit(false)
    }
}
