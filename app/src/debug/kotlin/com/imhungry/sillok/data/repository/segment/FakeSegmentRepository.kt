package com.imhungry.sillok.data.repository.segment

import android.os.Build
import androidx.annotation.RequiresApi
import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.model.segment.Segment
import com.imhungry.sillok.domain.repository.segment.SegmentRepository
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * debug 전용 SegmentRepository.
 *
 * 회의 시작 시점에 이미 존재하는 과거 세그먼트를 시뮬레이션합니다.
 * 여러 참가자가 대화하는 자연스러운 대화 흐름을 제공합니다.
 */
@Singleton
@RequiresApi(Build.VERSION_CODES.O)
class FakeSegmentRepository @Inject constructor() : SegmentRepository {

    companion object {
        // 더미 세그먼트 데이터 (userId, text)
        // FakeUserRepository의 사용자 ID와 매칭:
        // 1L: 조은경, 2L: 홍길동, 3L: 김영희, 4L: 박철수
        private val DUMMY_SEGMENTS = listOf(
            Pair(2L, "안녕하세요, 오늘 회의 시작하겠습니다."),
            Pair(2L, "첫 번째로 오늘의 아젠다를 확인해볼까요?"),
            Pair(1L, "네, 좋습니다. 프로젝트 목표와 범위부터 논의하면 될 것 같아요."),
            Pair(3L, "디자인 관점에서 보면 사용자 경험을 우선시해야 할 것 같습니다."),
            Pair(4L, "백엔드 API는 RESTful 방식으로 설계하는 게 좋을 것 같아요."),
            Pair(2L, "좋은 의견들이네요. 그럼 일정은 어떻게 생각하시나요?"),
            Pair(1L, "기획안은 다음 주까지 완료할 수 있을 것 같습니다."),
            Pair(4L, "개발은 기획안이 나온 후 2주 정도면 가능할 것 같아요."),
            Pair(3L, "디자인은 개발과 병행해서 진행하면 될 것 같습니다."),
            Pair(1L, "그럼 전체 일정은 약 3주 정도로 잡으면 되겠네요."),
            Pair(2L, "좋습니다. 그럼 역할 분담은 어떻게 할까요?"),
            Pair(3L, "저는 기획과 요구사항 정리를 담당하겠습니다."),
            Pair(3L, "디자인 시스템 구축과 UI 설계를 맡겠습니다."),
            Pair(4L, "백엔드 API 개발과 데이터베이스 설계를 담당하겠습니다."),
            Pair(1L, "프로젝트 관리와 전체적인 일정 조율을 맡겠습니다."),
            Pair(4L, "운영 환경 구축과 배포 관련 작업을 담당하겠습니다."),
            Pair(2L, "좋습니다. 그럼 다음 회의까지 각자 담당 업무를 정리해오시면 됩니다."),
            Pair(1L, "네, 알겠습니다. 오늘 회의는 여기서 마무리하겠습니다."),
            Pair(2L, "수고하셨습니다. 다음 주 같은 시간에 다시 모이겠습니다.")
        )
    }

    override suspend fun getSegments(
        meetingId: Long,
        page: Int,
        size: Int
    ): ApiResult<List<Segment>> {
        // 페이지네이션 처리
        val startIndex = page * size
        val endIndex = minOf(startIndex + size, DUMMY_SEGMENTS.size)
        
        if (startIndex >= DUMMY_SEGMENTS.size) {
            return ApiResult.Success(emptyList())
        }

        val segments = DUMMY_SEGMENTS.subList(startIndex, endIndex).mapIndexed { index, (userId, text) ->
            val segmentIndex = startIndex + index
            // 현재 시간 기준으로 과거 시간 생성 (회의 시작 후 시간 경과)
            // 첫 번째 세그먼트는 10초 후, 이후는 20-30초 간격
            val secondsAgo = (DUMMY_SEGMENTS.size - segmentIndex) * 25L + 10L
            val generatedTime = LocalDateTime.now(ZoneId.of("Asia/Seoul"))
                .minusSeconds(secondsAgo)
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

            Segment(
                id = meetingId * 10000L + segmentIndex, // 고유 ID 생성
                userId = userId,
                segmentOrder = segmentIndex,
                timestamp = generatedTime,
                text = text,
                lang = "ko" // 한국어
            )
        }

        return ApiResult.Success(segments)
    }
}
