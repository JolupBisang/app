package com.imhungry.sillok.data.repository.feedback

import android.os.Build
import androidx.annotation.RequiresApi
import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.model.feedback.Feedback
import com.imhungry.sillok.domain.repository.feedback.FeedbackRepository
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@RequiresApi(Build.VERSION_CODES.O)
class FakeFeedbackRepository @Inject constructor() : FeedbackRepository {

    companion object {
        // 더미 피드백 내용 목록
        private val DUMMY_FEEDBACK_COMMENTS = listOf(
            "이전 피드백 1",
            "이전 피드백 2",
            "이전 피드백 3",
            "이전 피드백 4",
            "이전 피드백 5",
            "이전 피드백 6",
            "이전 피드백 7",
            "이전 피드백 8",
            "이전 피드백 9",
            "이전 피드백 10",
            "이전 피드백 11",
            "이전 피드백 12",
            "이전 피드백 13",
            "이전 피드백 14",
            "이전 피드백 15"
        )
    }

    override suspend fun getFeedbacks(
        meetingId: Long,
        page: Int,
        size: Int
    ): ApiResult<List<Feedback>> {
        // 페이지네이션 처리
        val startIndex = page * size
        val endIndex = minOf(startIndex + size, DUMMY_FEEDBACK_COMMENTS.size)
        
        if (startIndex >= DUMMY_FEEDBACK_COMMENTS.size) {
            return ApiResult.Success(emptyList())
        }

        val feedbacks = DUMMY_FEEDBACK_COMMENTS.subList(startIndex, endIndex).mapIndexed { index, comment ->
            val feedbackIndex = startIndex + index
            // 현재 시간 기준으로 과거 시간 생성 (회의 시작 후 시간 경과)
            // 첫 번째 피드백은 5분 후, 이후는 2-3분 간격
            val minutesAgo = (DUMMY_FEEDBACK_COMMENTS.size - feedbackIndex) * 3L + 5L
            val generatedTime = LocalDateTime.now(ZoneId.of("Asia/Seoul"))
                .minusMinutes(minutesAgo)
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

            Feedback(
                id = meetingId * 1000L + feedbackIndex, // 고유 ID 생성
                comment = comment,
                generatedDateTime = generatedTime
            )
        }

        return ApiResult.Success(feedbacks)
    }
}
