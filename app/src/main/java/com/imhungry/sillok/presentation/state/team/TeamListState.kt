package com.imhungry.sillok.presentation.state.team

import com.imhungry.sillok.domain.model.team.TeamDetailSummary

data class TeamListState (
    val teams: List<TeamDetailSummary> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)