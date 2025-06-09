package com.imhungry.jjongseol.data.repository

import com.imhungry.jjongseol.data.model.response.SliceResponse
import com.imhungry.jjongseol.data.model.summary.response.SummaryListRes
import com.imhungry.jjongseol.data.network.api.SummaryApi
import javax.inject.Inject

sealed class SummaryResult<out T> {
    data class Success<T>(val data: T) : SummaryResult<T>()
    data class Error(val message: String) : SummaryResult<Nothing>()
    data class Exception(val throwable: Throwable) : SummaryResult<Nothing>()
}

class SummaryRepository @Inject constructor(
    private val summaryApi: SummaryApi
) {
    suspend fun getSummaries(
        meetingId: Long,
        isRecap: Boolean = false,
        page: Int = 0,
        size: Int = 30
    ): SummaryResult<SliceResponse<SummaryListRes>> {
        return try {
            val response = summaryApi.getSummaries(meetingId, isRecap, page, size)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) SummaryResult.Success(body)
                else SummaryResult.Error("응답이 비어있음")
            } else {
                SummaryResult.Error("서버 오류: ${response.code()}")
            }
        } catch (e: Exception) {
            SummaryResult.Exception(e)
        }
    }
}
