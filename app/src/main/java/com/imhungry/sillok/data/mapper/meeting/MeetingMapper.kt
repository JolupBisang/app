package com.imhungry.sillok.data.mapper.meeting

import android.os.Build
import androidx.annotation.RequiresApi
import com.imhungry.sillok.data.model.meeting.DuplicationCheckRes
import com.imhungry.sillok.data.model.meeting.MeetingDetailResDto
import com.imhungry.sillok.data.model.meeting.MeetingDetailSummaryResDto
import com.imhungry.sillok.data.model.meeting.MeetingReqDto
import com.imhungry.sillok.data.model.meeting.TeamTagAdditionReqDto
import com.imhungry.sillok.data.model.meeting.TeamTagRemovalReqDto
import com.imhungry.sillok.domain.model.agenda.Agenda
import com.imhungry.sillok.domain.model.meeting.AddTeamTagRequest
import com.imhungry.sillok.domain.model.meeting.CreateMeetingRequest
import com.imhungry.sillok.domain.model.meeting.DuplicatedMeeting
import com.imhungry.sillok.domain.model.meeting.Meeting
import com.imhungry.sillok.domain.model.meeting.MeetingDetailSummary
import com.imhungry.sillok.domain.model.meeting.RemoveTeamTagRequest
import com.imhungry.sillok.presentation.util.DateTimeUtils
import javax.inject.Inject

class MeetingMapper @Inject constructor() {
    fun toDto(request: CreateMeetingRequest): MeetingReqDto {
        return MeetingReqDto(
            title = request.title,
            location = request.location,
            scheduledStartTime = request.scheduledStartTime,
            targetTime = request.targetTime,
            restInterval = request.restInterval,
            restDuration = request.restDuration,
            participants = request.participants,
            agendas = request.agendas,
            teams = request.teams
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun toDomain(dto: MeetingDetailResDto): Meeting {
        return Meeting(
            meetingId = dto.meetingId,
            title = dto.title,
            location = dto.location,
            scheduledStartTime = dto.scheduledStartTime.take(19),
            actualStartTime = DateTimeUtils.utcToKoreaTime(dto.actualStartTime?: ""),
            scheduledEndTime = DateTimeUtils.utcToKoreaTime(dto.scheduledEndTime?: ""),
            targetTime = dto.targetTime / 60,
            restInterval = dto.restInterval,
            restDuration = dto.restDuration,
            meetingStatus = dto.meetingStatus,
            participants = dto.participants.map {
                Meeting.Participant(it.userId, it.email, it.role)
            },
            agendas = dto.agendas.map {
                Agenda(
                    agendaId = it.agendaId,
                    content = it.content,
                    isCompleted = it.isCompleted
                )
            },
            teamNames = dto.teamNames,
            isHost = dto.isHost
        )
    }

    fun toMeetingSummary(dto: MeetingDetailSummaryResDto): MeetingDetailSummary {
        return MeetingDetailSummary(
            id = dto.id,
            title = dto.title,
            scheduledStartTime = dto.scheduledStartTime.take(19),
            targetTime = dto.targetTime / 60,
            status = dto.status
        )
    }

    fun toDuplicatedMeetings(dto: DuplicationCheckRes): List<DuplicatedMeeting> {
        return dto.meetings.map { meetingDetail ->
            DuplicatedMeeting(
                title = meetingDetail.title,
                scheduledStartTime = meetingDetail.scheduledStartTime,
                scheduledEndTime = meetingDetail.scheduledEndTime
            )
        }
    }

    fun toDto(request: AddTeamTagRequest): TeamTagAdditionReqDto {
        return TeamTagAdditionReqDto(
            teamId = request.teamId
        )
    }

    fun toDto(request: RemoveTeamTagRequest): TeamTagRemovalReqDto {
        return TeamTagRemovalReqDto(
            teamId = request.teamId
        )
    }
}