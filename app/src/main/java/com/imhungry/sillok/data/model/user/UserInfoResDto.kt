package com.imhungry.sillok.data.model.user

data class UserInfoResDto(
    val id: Long,
    val email: String,
    val nickname: String,
    val pictureURL: String? // nullable로 변경 (API 응답에서 null일 수 있음)
)
