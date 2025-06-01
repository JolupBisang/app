package com.imhungry.jjongseol.data.repository

import com.imhungry.jjongseol.data.model.response.ErrorResponse
import com.imhungry.jjongseol.data.model.response.SuccessResponse
import com.imhungry.jjongseol.data.network.api.MeetingApi
import com.google.gson.Gson
import com.imhungry.jjongseol.data.model.meeting.MeetingStatus
import com.imhungry.jjongseol.data.model.meeting.request.MeetingStatusUpdateReq
import com.imhungry.jjongseol.data.model.meeting.response.MeetingDetailRes
import retrofit2.Response
import javax.inject.Inject

sealed class MeetingResult<out T> {
    data class Success<T>(val data: T) : MeetingResult<T>()
    data class Error(val message: String, val errorResponse: ErrorResponse? = null) : MeetingResult<Nothing>()
    data class Exception(val throwable: Throwable) : MeetingResult<Nothing>()
}

class MeetingRepository @Inject constructor(
    private val meetingApi: MeetingApi
) {
    suspend fun getMeetingDetail(meetingId: Long): MeetingResult<MeetingDetailRes> {
        return try {
            val response = meetingApi.getMeetingDetail(meetingId)
            handleApiResponse(response)
        } catch (e: Exception) {
            MeetingResult.Exception(e)
        }
    }

    suspend fun updateMeetingStatus(meetingId: Long, targetStatus: MeetingStatus): MeetingResult<Unit> {
        return try {
            val req = MeetingStatusUpdateReq(targetStatus.name)
            val response = meetingApi.updateMeetingStatus(meetingId, req)
            handleApiResponse(response)
        } catch (e: Exception) {
            MeetingResult.Exception(e)
        }
    }

    private inline fun <reified T> handleApiResponse(response: Response<SuccessResponse<T>>): MeetingResult<T> {
        return if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                MeetingResult.Success(body.data)
            } else {
                MeetingResult.Error("서버 응답이 올바르지 않습니다.", null)
            }
        } else {
            val errorBody = response.errorBody()?.string()
            val errorResponse = try {
                if (errorBody != null) Gson().fromJson(errorBody, ErrorResponse::class.java) else null
            } catch (e: Exception) { null }
            MeetingResult.Error(errorResponse?.message ?: "서버 오류 발생", errorResponse)
        }
    }
}
