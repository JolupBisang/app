package com.imhungry.sillok.domain.usecase.team

import com.imhungry.sillok.domain.repository.team.TeamRepository
import javax.inject.Inject

class GetTeamDetailUseCase @Inject constructor(
    private val repository: TeamRepository
) {
    suspend operator fun invoke(teamId: Long) =
        repository.getTeamDetail(teamId)
}

