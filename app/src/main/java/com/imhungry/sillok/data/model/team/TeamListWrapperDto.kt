package com.imhungry.sillok.data.model.team

data class TeamListWrapperDto(
    val teams: List<TeamListResDto>,
    val hasNext: Boolean
)

