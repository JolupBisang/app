package com.imhungry.sillok.domain.repository.team

import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.model.team.AddTeamMemberRequest
import com.imhungry.sillok.domain.model.team.CreateTeamRequest
import com.imhungry.sillok.domain.model.team.Team
import com.imhungry.sillok.domain.model.team.TeamListItem
import com.imhungry.sillok.domain.model.team.TeamMember

interface TeamRepository {
    suspend fun createTeam(request: CreateTeamRequest): ApiResult<Long>
    suspend fun getTeamDetail(teamId: Long): ApiResult<Team>
    suspend fun getMyTeams(): ApiResult<List<TeamListItem>>
    suspend fun addTeamMember(teamId: Long, request: AddTeamMemberRequest): ApiResult<Unit>
    suspend fun getTeamMembers(teamId: Long): ApiResult<List<TeamMember>>
}

