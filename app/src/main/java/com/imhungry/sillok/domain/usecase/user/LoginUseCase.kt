package com.imhungry.sillok.domain.usecase.user

import com.imhungry.sillok.data.local.TokenStore
import com.imhungry.sillok.data.local.UserStore
import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.model.user.User
import com.imhungry.sillok.domain.repository.user.UserRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val tokenStore: TokenStore,
    private val userStore: UserStore
) {

    companion object {
        private const val TAG = "LoginUseCase"
    }

    suspend operator fun invoke(token: String): ApiResult<User> {
        return try {
            tokenStore.saveTokens(token)
            val userResult = userRepository.getMyProfile()

            when (userResult) {
                is ApiResult.Success -> {
                    userStore.saveUser(userResult.data)
                    ApiResult.Success(userResult.data)
                }

                is ApiResult.Failure -> {
                    // 실패 시 토큰 삭제
                    tokenStore.clearTokens()
                    userResult
                }
            }
        } catch (e: Exception) {
            // 예외 발생 시 토큰 삭제
            tokenStore.clearTokens()
            ApiResult.Failure(e.localizedMessage ?: "로그인 처리 중 오류가 발생했습니다.")
        }
    }
}

