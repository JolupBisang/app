package com.imhungry.sillok.data.repository.meeting

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.imhungry.sillok.data.mapper.meeting.MeetingMapper
import com.imhungry.sillok.data.model.meeting.MeetingStatusUpdateReqDto
import com.imhungry.sillok.data.model.meeting.MeetingUpdateReqDto
import com.imhungry.sillok.data.model.meeting.TargetMeetingStatus
import com.imhungry.sillok.data.remote.meeting.MeetingApi
import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.model.meeting.CreateMeetingRequest
import com.imhungry.sillok.domain.model.meeting.DuplicatedMeeting
import com.imhungry.sillok.domain.model.meeting.Meeting
import com.imhungry.sillok.domain.model.meeting.MeetingDetailSummary
import com.imhungry.sillok.domain.repository.meeting.MeetingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MeetingRepositoryImpl @Inject constructor(
    private val api: MeetingApi,
    private val mapper: MeetingMapper
) : MeetingRepository {
    override suspend fun createMeeting(request: CreateMeetingRequest): ApiResult<Long> =
        withContext(Dispatchers.IO) {
            try {
                val dto = mapper.toDto(request)
                val res = api.createMeeting(dto)
                if (res.isSuccessful) {
                    ApiResult.Success(res.body()?.meetingId ?: -1L)
                } else {
                    ApiResult.Failure(res.message())
                }
            } catch (e: Exception) {
                ApiResult.Failure(e.localizedMessage ?: "알 수 없는 오류")
            }
        }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getMeetingDetail(meetingId: Long): ApiResult<Meeting> =
        withContext(Dispatchers.IO) {
            try {
                val res = api.getMeetingDetail(meetingId)
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

    override suspend fun getMeetings(year: Int, month: Int): ApiResult<List<MeetingDetailSummary>> =
        withContext(Dispatchers.IO) {
            try {
                val res = api.getMeetings(year, month)
                if (res.isSuccessful) {
                    val dtoList = res.body()
                    val summaries = dtoList?.map { mapper.toMeetingSummary(it) } ?: emptyList()
                    ApiResult.Success(summaries)
                } else {
                    ApiResult.Failure(res.message())
                }
            } catch (e: Exception) {
                ApiResult.Failure(e.localizedMessage ?: "알 수 없는 오류")
            }
        }

    override suspend fun updateMeetingStatus(
        meetingId: Long,
        targetStatus: TargetMeetingStatus
    ): ApiResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val res = api.updateMeetingStatus(meetingId, MeetingStatusUpdateReqDto(targetStatus))
            if (res.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Failure(res.message())
            }
        } catch (e: Exception) {
            ApiResult.Failure(e.localizedMessage ?: "알 수 없는 오류")
        }
    }

    override suspend fun updateMeeting(
        meetingId: Long,
        request: MeetingUpdateReqDto
    ): ApiResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val res = api.updateMeeting(meetingId, request)
            if (res.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Failure(res.message())
            }
        } catch (e: Exception) {
            ApiResult.Failure(e.localizedMessage ?: "알 수 없는 오류")
        }
    }

    override suspend fun checkDuplicatedTime(
        startTime: String,
        targetMinutes: Long
    ): ApiResult<List<DuplicatedMeeting>> = withContext(Dispatchers.IO) {
        try {
            val res = api.checkDuplicatedTime(startTime, targetMinutes)
            if (res.isSuccessful) {
                val dto = res.body()
                if (dto != null) {
                    val duplicatedMeetings = mapper.toDuplicatedMeetings(dto)
                    ApiResult.Success(duplicatedMeetings)
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
