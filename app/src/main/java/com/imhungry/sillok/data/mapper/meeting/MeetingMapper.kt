package com.imhungry.sillok.data.mapper.meeting

import com.imhungry.sillok.data.model.meeting.MeetingDetailResDto
import com.imhungry.sillok.data.model.meeting.MeetingDetailSummaryResDto
import com.imhungry.sillok.data.model.meeting.MeetingReqDto
import com.imhungry.sillok.domain.model.meeting.CreateMeetingRequest
import com.imhungry.sillok.domain.model.meeting.Meeting
import com.imhungry.sillok.domain.model.meeting.MeetingDetailSummary
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
            agendas = request.agendas
        )
    }

    fun toDomain(dto: MeetingDetailResDto): Meeting {
        return Meeting(
            meetingId = dto.meetingId,
            title = dto.title,
            location = dto.location,
            scheduledStartTime = dto.scheduledStartTime,
            targetTime = dto.targetTime,
            restInterval = dto.restInterval,
            restDuration = dto.restDuration,
            meetingStatus = dto.meetingStatus,
            participants = dto.participants.map {
                Meeting.Participant(it.userId, it.email)
            },
            isHost = dto.isHost
        )
    }

    fun toMeetingSummary(dto: MeetingDetailSummaryResDto): MeetingDetailSummary {
        return MeetingDetailSummary(
            id = dto.id,
            title = dto.title,
            scheduledStartTime = dto.scheduledStartTime,
            targetTime = dto.targetTime,
            status = dto.status
        )
    }
}