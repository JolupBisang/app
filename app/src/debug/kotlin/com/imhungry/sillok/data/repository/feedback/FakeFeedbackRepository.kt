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

/**
 * debug 전용 FeedbackRepository.
 *
 * 회의 진행 중 생성된 피드백을 시뮬레이션합니다.
 * 다양한 피드백 유형을 제공하여 UI 테스트에 유용합니다.
 */
@Singleton
@RequiresApi(Build.VERSION_CODES.O)
class FakeFeedbackRepository @Inject constructor() : FeedbackRepository {

    companion object {
        // 더미 피드백 내용 목록
        private val DUMMY_FEEDBACK_COMMENTS = listOf(
            "속도 좋습니다!",
            "요구사항 정리 항목 추가 제안",
            "회의 진행이 너무 빠릅니다. 조금 천천히 진행해주세요.",
            "좋은 의견들이 나오고 있습니다.",
            "다음 회의에서는 더 구체적인 계획을 논의하면 좋을 것 같습니다.",
            "시간 관리가 잘 되고 있습니다.",
            "아젠다 순서를 조정하면 더 효율적일 것 같습니다.",
            "참여율이 높아서 좋습니다.",
            "녹음 품질이 좋습니다.",
            "요약이 정확하게 나오고 있습니다.",
            "회의록 생성이 빠르게 진행되고 있습니다.",
            "다음 회의 일정을 미리 정하면 좋을 것 같습니다.",
            "의사결정이 명확해서 좋습니다.",
            "토론이 활발하게 이루어지고 있습니다.",
            "시간을 잘 지키고 있어서 좋습니다."
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
