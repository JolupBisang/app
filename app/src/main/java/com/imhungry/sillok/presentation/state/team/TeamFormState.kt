package com.imhungry.sillok.presentation.state.team

data class TeamFormState(
    val teamName: String = "",
    val memberEmails: List<String> = emptyList(),
    val emailInput: String = "",
    val showMemberInvite: Boolean = false,
    val showValidationErrors: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)
