package com.imhungry.sillok.domain.usecase.user

import android.util.Log
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
        Log.d(TAG, "[invoke] 로그인 처리 시작")
        return try {
            // 1단계: 기존 데이터 클리어 (새로운 로그인을 위해 이전 세션 정보 제거)
            Log.d(TAG, "[invoke] 1단계: 기존 토큰 및 사용자 정보 삭제")
            tokenStore.clearTokens()
            userStore.clearUser()
            
            // 2단계: 새 토큰 저장 (API 요청 시 사용할 토큰)
            Log.d(TAG, "[invoke] 2단계: 새 토큰 저장")
            tokenStore.saveTokens(token)
            
            // 3단계: 저장된 토큰을 사용하여 사용자 정보 조회
            // AuthInterceptor가 자동으로 Authorization 헤더에 토큰을 추가합니다
            Log.d(TAG, "[invoke] 3단계: 사용자 정보 조회 시작")
            val userResult = userRepository.getMyProfile()
            Log.d(TAG, "[invoke] 사용자 정보 조회 결과: ${userResult::class.simpleName}")

            when (userResult) {
                is ApiResult.Success -> {
                    // 4단계: 로그인 성공 - 사용자 정보를 로컬에 저장
                    Log.d(TAG, "[invoke] 4단계: 로그인 성공 - 사용자 정보 저장")
                    Log.d(TAG, "[invoke] 사용자 정보: id=${userResult.data.id}, email=${userResult.data.email}, nickname=${userResult.data.nickname}")
                    userStore.saveUser(userResult.data)
                    Log.d(TAG, "[invoke] 로그인 처리 완료 (성공)")
                    ApiResult.Success(userResult.data)
                }

                is ApiResult.Failure -> {
                    // 로그인 실패 시 저장된 토큰 삭제 (보안 및 일관성 유지)
                    Log.e(TAG, "[invoke] 로그인 실패: ${userResult.message}")
                    Log.d(TAG, "[invoke] 저장된 토큰 삭제")
                    tokenStore.clearTokens()
                    Log.d(TAG, "[invoke] 로그인 처리 완료 (실패)")
                    userResult
                }
            }
        } catch (e: Exception) {
            // 예외 발생 시 모든 데이터 삭제 및 에러 반환
            Log.e(TAG, "[invoke] 예외 발생: ${e.message}", e)
            Log.d(TAG, "[invoke] 예외 발생으로 인한 토큰 및 사용자 정보 삭제")
            tokenStore.clearTokens()
            userStore.clearUser()
            Log.d(TAG, "[invoke] 로그인 처리 완료 (예외)")
            ApiResult.Failure(e.localizedMessage ?: "로그인 처리 중 오류가 발생했습니다.")
        }
    }
}

