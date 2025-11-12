package com.imhungry.sillok.domain.usecase.team

import com.imhungry.sillok.domain.model.team.CreateTeamRequest
import com.imhungry.sillok.domain.repository.team.TeamRepository
import javax.inject.Inject

class CreateTeamUseCase @Inject constructor(
    private val repository: TeamRepository
) {
    suspend operator fun invoke(request: CreateTeamRequest) =
        repository.createTeam(request)
}

