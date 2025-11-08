package com.imhungry.sillok.domain.repository.meeting

import com.imhungry.sillok.data.model.meeting.MeetingUpdateReqDto
import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.model.meeting.CreateMeetingRequest
import com.imhungry.sillok.domain.model.meeting.Meeting
import com.imhungry.sillok.domain.model.meeting.MeetingDetailSummary

import com.imhungry.sillok.data.model.meeting.TargetMeetingStatus

interface MeetingRepository {
    suspend fun createMeeting(request: CreateMeetingRequest): ApiResult<Long>
    suspend fun getMeetingDetail(meetingId: Long): ApiResult<Meeting>
    suspend fun getMeetings(year: Int, month: Int): ApiResult<List<MeetingDetailSummary>>
    suspend fun updateMeetingStatus(meetingId: Long, targetStatus: TargetMeetingStatus): ApiResult<Unit>
    suspend fun updateMeeting(meetingId: Long, request: MeetingUpdateReqDto): ApiResult<Unit>
}
