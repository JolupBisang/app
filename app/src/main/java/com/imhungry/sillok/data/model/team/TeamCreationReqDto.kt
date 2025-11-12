package com.imhungry.sillok.data.model.team

data class TeamCreationReqDto(
    val teamName: String,
    val memberEmails: List<String>
)

