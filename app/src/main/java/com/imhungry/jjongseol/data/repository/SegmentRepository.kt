package com.imhungry.jjongseol.data.repository

import com.imhungry.jjongseol.data.model.response.SliceResponse
import com.imhungry.jjongseol.data.model.segment.response.SegmentListRes
import com.imhungry.jjongseol.data.network.api.SegmentApi
import retrofit2.Response
import javax.inject.Inject

sealed class SegmentResult<out T> {
    data class Success<T>(val data: T) : SegmentResult<T>()
    data class Error(val message: String) : SegmentResult<Nothing>()
    data class Exception(val throwable: Throwable) : SegmentResult<Nothing>()
}

class SegmentRepository @Inject constructor(
    private val segmentApi: SegmentApi
) {
    suspend fun getSegments(meetingId: Long, page: Int = 0, size: Int = 40): SegmentResult<SliceResponse<SegmentListRes>> {
        return try {
            val response = segmentApi.getSegments(meetingId, page, size)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) SegmentResult.Success(body)
                else SegmentResult.Error("응답이 비어있음")
            } else {
                SegmentResult.Error("서버 오류: ${response.code()}")
            }
        } catch (e: Exception) {
            SegmentResult.Exception(e)
        }
    }
}
