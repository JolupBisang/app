package com.imhungry.sillok.domain.repository.meeting

import com.imhungry.sillok.data.model.meeting.MeetingUpdateReqDto
import com.imhungry.sillok.data.model.meeting.TargetMeetingStatus
import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.model.meeting.AddTeamTagRequest
import com.imhungry.sillok.domain.model.meeting.CreateMeetingRequest
import com.imhungry.sillok.domain.model.meeting.DuplicatedMeeting
import com.imhungry.sillok.domain.model.meeting.Meeting
import com.imhungry.sillok.domain.model.meeting.MeetingDetailSummary
import com.imhungry.sillok.domain.model.meeting.MeetingSearchSlice
import com.imhungry.sillok.domain.model.meeting.RemoveTeamTagRequest

interface MeetingRepository {
    suspend fun createMeeting(request: CreateMeetingRequest): ApiResult<Long>
    suspend fun getMeetingDetail(meetingId: Long): ApiResult<Meeting>
    suspend fun getMeetings(
        year: Int? = null,
        month: Int? = null,
        title: String? = null,
        page: Int = 0,
        size: Int = 20
    ): ApiResult<MeetingSearchSlice>
    suspend fun getMeetings2(year: Int, month: Int): ApiResult<List<MeetingDetailSummary>>
    suspend fun updateMeetingStatus(
        meetingId: Long,
        targetStatus: TargetMeetingStatus
    ): ApiResult<Unit>

    suspend fun updateMeeting(meetingId: Long, request: MeetingUpdateReqDto): ApiResult<Unit>
    suspend fun checkDuplicatedTime(startTime: String, targetMinutes: Long): ApiResult<List<DuplicatedMeeting>>
    suspend fun addTeamTag(meetingId: Long, request: AddTeamTagRequest): ApiResult<Unit>
    suspend fun removeTeamTag(meetingId: Long, request: RemoveTeamTagRequest): ApiResult<Unit>
    suspend fun searchMeetings(title: String, page: Int, size: Int): ApiResult<MeetingSearchSlice>
}
