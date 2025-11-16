package com.imhungry.sillok.data.mapper.user

import com.imhungry.sillok.data.model.user.UserInfoResDto
import com.imhungry.sillok.domain.model.user.User
import javax.inject.Inject

class UserMapper @Inject constructor() {
    fun toDomain(dto: UserInfoResDto): User {
        return User(
            id = dto.id,
            email = dto.email,
            nickname = dto.nickname,
            pictureURL = dto.pictureURL ?: "" // null일 경우 빈 문자열로 처리
        )
    }
}