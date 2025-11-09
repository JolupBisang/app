package com.imhungry.sillok.data.repository.feedback

import com.imhungry.sillok.data.mapper.feedback.FeedbackMapper
import com.imhungry.sillok.data.remote.feedback.FeedbackApi
import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.model.feedback.Feedback
import com.imhungry.sillok.domain.repository.feedback.FeedbackRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FeedbackRepositoryImpl @Inject constructor(
    private val api: FeedbackApi,
    private val mapper: FeedbackMapper
) : FeedbackRepository {

    override suspend fun getFeedbacks(
        meetingId: Long,
        page: Int,
        size: Int
    ): ApiResult<List<Feedback>> = withContext(Dispatchers.IO) {
        try {
            val res = api.getFeedbacks(meetingId, page, size)
            if (res.isSuccessful) {
                val dto = res.body()
                if (dto != null) {
                    val feedbacks = dto.content.map { mapper.toDomain(it) }
                    ApiResult.Success(feedbacks)
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
