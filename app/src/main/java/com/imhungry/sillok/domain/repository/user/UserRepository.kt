package com.imhungry.sillok.domain.repository.user

import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.model.user.User

interface UserRepository {
    suspend fun getUserInfo(email: String): ApiResult<User>
    suspend fun getMyProfile(): ApiResult<User>
}
