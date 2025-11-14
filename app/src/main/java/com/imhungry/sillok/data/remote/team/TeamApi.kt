package com.imhungry.sillok.data.remote.team

import com.imhungry.sillok.data.model.team.TeamCreationReqDto
import com.imhungry.sillok.data.model.team.TeamCreationResDto
import com.imhungry.sillok.data.model.team.TeamDetailResDto
import com.imhungry.sillok.data.model.team.TeamListResDto
import com.imhungry.sillok.data.model.team.TeamListWrapperDto
import com.imhungry.sillok.data.model.team.TeamMemberAdditionReqDto
import com.imhungry.sillok.data.model.team.TeamMemberAdditionResDto
import com.imhungry.sillok.data.model.team.TeamMemberResDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface TeamApi {
    @POST("/api/v1/teams")
    suspend fun createTeam(
        @Body request: TeamCreationReqDto
    ): Response<TeamCreationResDto>

    @GET("/api/v1/teams/{teamId}")
    suspend fun getTeamDetail(
        @Path("teamId") teamId: Long
    ): Response<TeamDetailResDto>

    @GET("/api/v1/teams")
    suspend fun getMyTeams(): Response<TeamListWrapperDto>

    @POST("/api/v1/teams/{teamId}/members")
    suspend fun addTeamMember(
        @Path("teamId") teamId: Long,
        @Body request: TeamMemberAdditionReqDto
    ): Response<TeamMemberAdditionResDto>

    @GET("/api/v1/teams/{teamId}/members")
    suspend fun getTeamMembers(
        @Path("teamId") teamId: Long
    ): Response<TeamMemberResDto>
}

