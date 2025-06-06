package com.imhungry.jjongseol.data.repository

import com.imhungry.jjongseol.data.model.response.ErrorResponse
import com.imhungry.jjongseol.data.model.response.SuccessResponse
import com.imhungry.jjongseol.data.network.api.UserApi
import com.google.gson.Gson
import com.imhungry.jjongseol.data.model.user.response.UserInfoResponse
import retrofit2.Response
import javax.inject.Inject

sealed class UserResult<out T> {
    data class Success<T>(val data: T) : UserResult<T>()
    data class Error(val message: String, val errorResponse: ErrorResponse? = null) : UserResult<Nothing>()
    data class Exception(val throwable: Throwable) : UserResult<Nothing>()
}

class UserRepository @Inject constructor(
    private val userApi: UserApi
) {
    suspend fun getUserInfo(email: String): UserResult<UserInfoResponse> {
        return try {
            val response = userApi.getUserInfo(email)
            handleApiResponse(response)
        } catch (e: Exception) {
            UserResult.Exception(e)
        }
    }

    suspend fun getMyNickname2(): UserResult<String> {
        return try {
            val response = userApi.getMyNickname2()
            handleApiResponse(response)
        } catch (e: Exception) {
            UserResult.Exception(e)
        }
    }

    private inline fun <reified T> handleApiResponse(response: Response<SuccessResponse<T>>): UserResult<T> {
        return if (response.isSuccessful) {
            val body = response.body()
            if (body != null && body.data != null) {
                UserResult.Success(body.data)
            } else {
                UserResult.Error("서버 응답이 올바르지 않습니다.", null)
            }
        } else {
            val errorBody = response.errorBody()?.string()
            val errorResponse = try {
                if (errorBody != null) Gson().fromJson(errorBody, ErrorResponse::class.java) else null
            } catch (e: Exception) {
                null
            }
            UserResult.Error(errorResponse?.message ?: "서버 오류 발생", errorResponse)
        }
    }
}
