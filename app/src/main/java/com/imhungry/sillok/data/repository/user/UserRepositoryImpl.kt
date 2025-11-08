package com.imhungry.sillok.data.repository.user

import com.imhungry.sillok.data.mapper.user.UserMapper
import com.imhungry.sillok.data.remote.user.UserApi
import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.model.user.User
import com.imhungry.sillok.domain.repository.user.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val api: UserApi,
    private val mapper: UserMapper
) : UserRepository {

    override suspend fun getUserInfo(email: String): ApiResult<User> = withContext(Dispatchers.IO) {
        try {
            val res = api.getUserInfo(email)
            if (res.isSuccessful) {
                val dto = res.body()
                if (dto != null) {
                    ApiResult.Success(mapper.toDomain(dto))
                } else {
                    ApiResult.Failure("응답 파싱 오류")
                }
            } else {
                ApiResult.Failure(res.message())
            }
        } catch (e: Exception) {
            ApiResult.Failure(e.localizedMessage ?: "알 수 없는 오류")
        }
    }

    override suspend fun getMyProfile(): ApiResult<User> = withContext(Dispatchers.IO) {
        try {
            val res = api.getMyProfile()
            if (res.isSuccessful) {
                val dto = res.body()
                if (dto != null) {
                    ApiResult.Success(mapper.toDomain(dto))
                } else {
                    ApiResult.Failure("응답 파싱 오류")
                }
            } else if (res.code() == 401) {
                ApiResult.Failure("토큰이 만료되었습니다. 다시 로그인해주세요.")
            } else {
                ApiResult.Failure(res.message())
            }
        } catch (e: Exception) {
            ApiResult.Failure(e.localizedMessage ?: "알 수 없는 오류")
        }
    }
}