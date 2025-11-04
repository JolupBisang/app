package com.imhungry.sillok.domain.usecase.user

import android.util.Log
import com.imhungry.sillok.data.local.TokenStore
import com.imhungry.sillok.data.local.UserStore
import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.model.user.User
import com.imhungry.sillok.domain.repository.user.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
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
            // 1. 토큰 저장
            tokenStore.saveTokens(token)
            val userResult = userRepository.getMyProfile()
            
            when (userResult) {
                is ApiResult.Success -> {
                    // 3. 사용자 정보 저장
                    userStore.saveUser(userResult.data)

                    // 4. 파이어베이스에 사용자 이메일 저장 (이미 없는 경우에만)
                    try {
                        val db = FirebaseFirestore.getInstance()
                        val email = userResult.data.email
                        val uid = userResult.data.id.toString()
                        // 이메일 기준으로 존재 여부 확인
                        val existing = db.collection("users")
                            .whereEqualTo("email", email)
                            .limit(1)
                            .get()
                            .await()

                        if (existing.isEmpty) {
                            val data = hashMapOf(
                                "email" to email,
                                "createdAt" to System.currentTimeMillis(),
                                "hasNewMeeting" to false, // meeting이 추가되었으면 홈 새로고침
                                "meetingStarted" to false, // meeting 시작되면 바로 회의 중 화면으로 이동
                                "startedMeetingId" to 0L // 시작된 회의 ID
                            )
                            db.collection("users").document(uid).set(data)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "파이어베이스 저장 중 예외: ${e.message}")
                    }
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

