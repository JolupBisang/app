package com.imhungry.jjongseol.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imhungry.jjongseol.data.model.user.response.UserInfoResponse
import com.imhungry.jjongseol.data.network.api.UserApi
import com.imhungry.jjongseol.data.repository.UserRepository
import com.imhungry.jjongseol.data.repository.UserResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    val userApi: UserApi,
    private val userRepository: UserRepository
) : ViewModel() {
    private val _userInfo = MutableStateFlow<UserInfoResponse?>(null)
    val userInfo: StateFlow<UserInfoResponse?> = _userInfo

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadUserInfo(email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = userRepository.getUserInfo(email)) {
                is UserResult.Success -> {
                    _userInfo.value = result.data
                    _errorMessage.value = null
                }
                is UserResult.Error -> {
                    _errorMessage.value = result.errorResponse?.message ?: result.message
                }
                is UserResult.Exception -> {
                    _errorMessage.value = result.throwable.message ?: "네트워크 오류"
                }

                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun loadMyProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = userRepository.getMyProfile()) {
                is UserResult.Success -> {
                    _userInfo.value = result.data
                    _errorMessage.value = null
                }
                is UserResult.Error -> {
                    _errorMessage.value = result.errorResponse?.message ?: result.message
                }
                is UserResult.Exception -> {
                    _errorMessage.value = result.throwable.message ?: "네트워크 오류"
                }

                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}
