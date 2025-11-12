package com.imhungry.sillok.domain.usecase.team

import com.imhungry.sillok.domain.repository.team.TeamRepository
import javax.inject.Inject

class GetMyTeamsUseCase @Inject constructor(
    private val repository: TeamRepository
) {
    suspend operator fun invoke() =
        repository.getMyTeams()
}

