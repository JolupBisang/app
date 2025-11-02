package com.imhungry.sillok.data.model.user

data class UserInfoResDto(
    val id: Long,
    val email: String,
    val nickname: String,
    val profileImage: String? = null
)
