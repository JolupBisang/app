package com.imhungry.sillok.data.mapper.team

import com.imhungry.sillok.data.model.team.MemberDto
import com.imhungry.sillok.data.model.team.TeamCreationReqDto
import com.imhungry.sillok.data.model.team.TeamDetailResDto
import com.imhungry.sillok.data.model.team.TeamListResDto
import com.imhungry.sillok.data.model.team.TeamMemberAdditionReqDto
import com.imhungry.sillok.data.model.team.TeamMemberResDto
import com.imhungry.sillok.domain.model.team.AddTeamMemberRequest
import com.imhungry.sillok.domain.model.team.CreateTeamRequest
import com.imhungry.sillok.domain.model.team.Team
import com.imhungry.sillok.domain.model.team.TeamListItem
import com.imhungry.sillok.domain.model.team.TeamMember
import javax.inject.Inject

class TeamMapper @Inject constructor() {
    fun toDto(request: CreateTeamRequest): TeamCreationReqDto {
        return TeamCreationReqDto(
            teamName = request.teamName,
            memberEmails = request.memberEmails
        )
    }

    fun toDomain(dto: TeamDetailResDto): Team {
        return Team(
            teamId = dto.teamId,
            teamName = dto.teamName
        )
    }

    fun toTeamListItem(dto: TeamListResDto): TeamListItem {
        return TeamListItem(
            teamId = dto.teamId,
            teamName = dto.teamName,
            meetingName = dto.meetingName,
            scheduledStartTime = dto.scheduledStartTime.take(19),
            scheduledEndTime = dto.scheduledEndTime.take(19)
        )
    }

    fun toDto(request: AddTeamMemberRequest): TeamMemberAdditionReqDto {
        return TeamMemberAdditionReqDto(
            memberEmail = request.memberEmail
        )
    }

    fun toTeamMember(dto: MemberDto): TeamMember {
        return TeamMember(
            id = dto.id,
            name = dto.name,
            pictureURL = dto.pictureURL
        )
    }

    fun toTeamMembers(dto: TeamMemberResDto): List<TeamMember> {
        return dto.members.map { toTeamMember(it) }
    }
}

