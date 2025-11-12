package com.imhungry.sillok.domain.usecase.user

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.imhungry.sillok.data.local.TokenStore
import com.imhungry.sillok.data.local.UserStore
import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.model.user.User
import com.imhungry.sillok.domain.repository.user.UserRepository
import kotlinx.coroutines.flow.first
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

                    // 4. 파이어베이스에 사용자 정보 업데이트 (항상 업데이트)
                    try {
                        val db = FirebaseFirestore.getInstance()
                        val email = userResult.data.email
                        val uid = userResult.data.id.toString()
                        
                        // DataStore에서 FCM 토큰 가져오기
                        val fcmToken = try {
                            tokenStore.fcmToken.first()
                        } catch (e: Exception) {
                            Log.w(TAG, "DataStore에서 FCM 토큰 가져오기 실패: ${e.message}")
                            null
                        }
                        
                        // 항상 업데이트 (기존 문서가 있으면 업데이트, 없으면 생성)
                        val updateData = hashMapOf<String, Any>(
                            "email" to email,
                            "hasNewMeeting" to false, // meeting이 추가되었으면 홈 새로고침
                            "newMeetingId" to -1L,
                            "hasNewTeam" to false,
                            "newTeamId" to -1L,
                            "meetingStarted" to false, // meeting 시작되면 바로 회의 중 화면으로 이동
                            "startedMeetingId" to -1L // 시작된 회의 ID
                        )
                        
                        // FCM 토큰이 있으면 추가
//                        fcmToken?.let {
//                            updateData["fcmToken"] = it
//                        }
                        
                        db.collection("users").document(uid).set(updateData)
                        Log.d(TAG, "파이어베이스 사용자 정보 업데이트 완료: userId=$uid, fcmToken=${fcmToken != null}")
                    } catch (e: Exception) {
                        Log.e(TAG, "파이어베이스 저장 중 예외: ${e.message}", e)
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

