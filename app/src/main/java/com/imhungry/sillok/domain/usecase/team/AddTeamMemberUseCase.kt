package com.imhungry.sillok.domain.usecase.team

import com.imhungry.sillok.domain.model.team.AddTeamMemberRequest
import com.imhungry.sillok.domain.repository.team.TeamRepository
import javax.inject.Inject

class AddTeamMemberUseCase @Inject constructor(
    private val repository: TeamRepository
) {
    suspend operator fun invoke(teamId: Long, request: AddTeamMemberRequest) =
        repository.addTeamMember(teamId, request)
}

