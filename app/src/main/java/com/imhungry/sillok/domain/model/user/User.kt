package com.imhungry.sillok.domain.model.user

import com.imhungry.sillok.presentation.util.ProfileUtils

data class User(
    val id: Long,
    val email: String,
    val nickname: String,
    val pictureURL: String
)
