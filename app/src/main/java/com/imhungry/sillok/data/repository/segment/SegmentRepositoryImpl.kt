package com.imhungry.sillok.data.repository.segment

import android.os.Build
import androidx.annotation.RequiresApi
import com.imhungry.sillok.data.mapper.segment.SegmentMapper
import com.imhungry.sillok.data.remote.segment.SegmentApi
import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.model.segment.Segment
import com.imhungry.sillok.domain.repository.segment.SegmentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SegmentRepositoryImpl @Inject constructor(
    private val api: SegmentApi,
    private val mapper: SegmentMapper
) : SegmentRepository {

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getSegments(
        meetingId: Long,
        page: Int,
        size: Int,
        direction: String
    ): ApiResult<List<Segment>> = withContext(Dispatchers.IO) {
        try {
            val res = api.getSegments(meetingId, page, size, sort = "order", direction = direction)
            if (res.isSuccessful) {
                val dto = res.body()
                if (dto != null) {
                    val segments = dto.content.map { mapper.toDomain(it) }
                    ApiResult.Success(segments)
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
    
    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getTotalSegmentCount(meetingId: Long): ApiResult<Long?> = withContext(Dispatchers.IO) {
        try {
            // 첫 페이지를 작은 size로 요청하여 totalElements 확인
            val res = api.getSegments(meetingId, page = 0, size = 1, sort = "order", direction = "desc")
            if (res.isSuccessful) {
                val dto = res.body()
                if (dto != null) {
                    // totalElements가 있으면 사용, 없으면 null 반환
                    val totalElements = dto.totalElements
                    ApiResult.Success(totalElements)
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
}
