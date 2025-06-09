package com.imhungry.jjongseol.data.repository

import com.imhungry.jjongseol.data.model.feedback.response.FeedbackListRes
import com.imhungry.jjongseol.data.model.response.SliceResponse
import com.imhungry.jjongseol.data.network.api.FeedbackApi
import javax.inject.Inject

sealed class FeedbackResult<out T> {
    data class Success<T>(val data: T) : FeedbackResult<T>()
    data class Error(val message: String) : FeedbackResult<Nothing>()
    data class Exception(val throwable: Throwable) : FeedbackResult<Nothing>()
}

class FeedbackRepository @Inject constructor(
    private val feedbackApi: FeedbackApi
) {
    suspend fun getFeedbacks(
        meetingId: Long,
        page: Int = 0,
        size: Int = 30
    ): FeedbackResult<SliceResponse<FeedbackListRes>> {
        return try {
            val response = feedbackApi.getFeedbacks(meetingId, page, size)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) FeedbackResult.Success(body)
                else FeedbackResult.Error("응답이 비어있음")
            } else {
                FeedbackResult.Error("서버 오류: ${response.code()}")
            }
        } catch (e: Exception) {
            FeedbackResult.Exception(e)
        }
    }
}
