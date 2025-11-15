package com.imhungry.sillok.presentation.state.mypage

data class MyPageState(
    val nickname: String = "",
    val profileImage: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)
