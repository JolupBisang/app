package com.imhungry.sillok.domain.usecase.user

import com.imhungry.sillok.domain.repository.user.UserRepository
import javax.inject.Inject

class GetUserInfoUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(email: String) = repository.getUserInfo(email)
}