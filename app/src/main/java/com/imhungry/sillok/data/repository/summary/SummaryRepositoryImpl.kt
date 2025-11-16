package com.imhungry.sillok.data.repository.summary

import com.imhungry.sillok.data.mapper.summary.SummaryMapper
import com.imhungry.sillok.data.remote.summary.SummaryApi
import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.model.summary.Summary
import com.imhungry.sillok.domain.repository.summary.SummaryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SummaryRepositoryImpl @Inject constructor(
    private val api: SummaryApi,
    private val mapper: SummaryMapper
) : SummaryRepository {

    override suspend fun getSummaries(
        meetingId: Long,
        isRecap: Boolean,
        page: Int,
        size: Int
    ): ApiResult<List<Summary>> = withContext(Dispatchers.IO) {
        try {
            val res = api.getSummaries(meetingId, isRecap, page, size, sort = "generatedDateTime", direction = "asc")
            if (res.isSuccessful) {
                val dto = res.body()
                if (dto != null) {
                    val summaries = dto.content.map { mapper.toDomain(it) }
                    ApiResult.Success(summaries)
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
