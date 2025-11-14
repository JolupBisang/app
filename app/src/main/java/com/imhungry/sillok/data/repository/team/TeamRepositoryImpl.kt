package com.imhungry.sillok.data.repository.team

import com.imhungry.sillok.data.mapper.team.TeamMapper
import com.imhungry.sillok.data.remote.team.TeamApi
import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.model.team.AddTeamMemberRequest
import com.imhungry.sillok.domain.model.team.CreateTeamRequest
import com.imhungry.sillok.domain.model.team.Team
import com.imhungry.sillok.domain.model.team.TeamListItem
import com.imhungry.sillok.domain.model.team.TeamMember
import com.imhungry.sillok.domain.repository.team.TeamRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TeamRepositoryImpl @Inject constructor(
    private val api: TeamApi,
    private val mapper: TeamMapper
) : TeamRepository {
    override suspend fun createTeam(request: CreateTeamRequest): ApiResult<Long> =
        withContext(Dispatchers.IO) {
            try {
                val dto = mapper.toDto(request)
                val res = api.createTeam(dto)
                if (res.isSuccessful) {
                    ApiResult.Success(res.body()?.teamId ?: -1L)
                } else {
                    ApiResult.Failure(res.message())
                }
            } catch (e: Exception) {
                ApiResult.Failure(e.localizedMessage ?: "알 수 없는 오류")
            }
        }

    override suspend fun getTeamDetail(teamId: Long): ApiResult<Team> =
        withContext(Dispatchers.IO) {
            try {
                val res = api.getTeamDetail(teamId)
                if (res.isSuccessful) {
                    val dto = res.body()
                    if (dto != null) {
                        ApiResult.Success(mapper.toDomain(dto))
                    } else {
                        ApiResult.Failure("응답 파싱 오류")
                    }
                } else {
                    ApiResult.Failure(res.message())
                }
            } catch (e: Exception) {
                ApiResult.Failure(e.localizedMessage ?: "알 수 없는 오류")
            }
        }

    override suspend fun getMyTeams(): ApiResult<List<TeamListItem>> =
        withContext(Dispatchers.IO) {
            try {
                val res = api.getMyTeams()
                if (res.isSuccessful) {
                    val wrapper = res.body()
                    val dtoList = wrapper?.teams ?: emptyList()
                    val teams = dtoList.map { mapper.toTeamListItem(it) }
                    ApiResult.Success(teams)
                } else {
                    ApiResult.Failure(res.message())
                }
            } catch (e: Exception) {
                ApiResult.Failure(e.localizedMessage ?: "알 수 없는 오류")
            }
        }

    override suspend fun addTeamMember(
        teamId: Long,
        request: AddTeamMemberRequest
    ): ApiResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val dto = mapper.toDto(request)
            val res = api.addTeamMember(teamId, dto)
            if (res.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Failure(res.message())
            }
        } catch (e: Exception) {
            ApiResult.Failure(e.localizedMessage ?: "알 수 없는 오류")
        }
    }

    override suspend fun getTeamMembers(teamId: Long): ApiResult<List<TeamMember>> =
        withContext(Dispatchers.IO) {
            try {
                val res = api.getTeamMembers(teamId)
                if (res.isSuccessful) {
                    val dto = res.body()
                    if (dto != null) {
                        val members = mapper.toTeamMembers(dto)
                        ApiResult.Success(members)
                    } else {
                        ApiResult.Success(emptyList())
                    }
                } else {
                    ApiResult.Failure(res.message())
                }
            } catch (e: Exception) {
                ApiResult.Failure(e.localizedMessage ?: "알 수 없는 오류")
            }
        }
}

