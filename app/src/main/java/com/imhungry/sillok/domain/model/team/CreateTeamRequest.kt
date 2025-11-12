package com.imhungry.sillok.domain.model.team

data class CreateTeamRequest(
    val teamName: String,
    val memberEmails: List<String>
)

