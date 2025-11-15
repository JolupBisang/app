package com.imhungry.sillok.presentation.viewmodel.mypage

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imhungry.sillok.data.local.TokenStore
import com.imhungry.sillok.data.local.UserStore
import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.usecase.user.GetMyProfileUseCase
import com.imhungry.sillok.presentation.state.mypage.MyPageState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val getMyProfileUseCase: GetMyProfileUseCase,
    private val tokenStore: TokenStore,
    private val userStore: UserStore
) : ViewModel() {

    companion object {
        private const val TAG = "MyPageViewModel"
    }

    private val _state = MutableStateFlow(MyPageState())
    val state: StateFlow<MyPageState> = _state.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                when (val result = getMyProfileUseCase()) {
                    is ApiResult.Success -> {
                        val user = result.data
                        Log.d(TAG, "프로필 로드 성공: nickname=${user.nickname}, pictureURL=${user.pictureURL}")
                        _state.update {
                            it.copy(
                                nickname = user.nickname,
                                profileImage = user.pictureURL,
                                isLoading = false,
                                error = null
                            )
                        }
                    }
                    is ApiResult.Failure -> {
                        Log.e(TAG, "프로필 로드 실패: ${result.message}")
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = result.message ?: "프로필을 불러오는데 실패했습니다."
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "프로필 로드 예외 발생: ${e.message}", e)
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "오류가 발생했습니다."
                    )
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                tokenStore.clearTokens()
                userStore.clearUser()
                Log.d(TAG, "로그아웃 완료: 토큰 및 사용자 정보 삭제")
            } catch (e: Exception) {
                Log.e(TAG, "로그아웃 중 오류 발생: ${e.message}", e)
            }
        }
    }
}
