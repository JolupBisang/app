package com.imhungry.sillok.presentation.state.team

data class TeamMember(
    val id: Long,
    val nickname: String,
    val email: String,
    val profileImage: String? = null,
)

data class TeamDetailState(
    val teamName: String = "",
    val teamDescription: String = "",
    val members: List<TeamMember> = emptyList(),
    val currentUserId: Long? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
