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

@Singleton
@RequiresApi(Build.VERSION_CODES.O)
class FakeSegmentRepository @Inject constructor() : SegmentRepository {

    companion object {
        private val DUMMY_SEGMENTS = (0..119).map { index ->
            val userId = ((index % 4) + 1).toLong() // 1, 2, 3, 4 순환
            Pair(userId, "이전 대화 $index")
        }
    }

    override suspend fun getSegments(
        meetingId: Long,
        page: Int,
        size: Int,
        direction: String
    ): ApiResult<List<Segment>> {
        // direction에 따라 정렬된 리스트 준비
        val sortedSegments = DUMMY_SEGMENTS.reversed()
        
        // 페이지네이션 처리
        val startIndex = page * size
        val endIndex = minOf(startIndex + size, sortedSegments.size)
        
        if (startIndex >= sortedSegments.size) {
            return ApiResult.Success(emptyList())
        }

        val segments = sortedSegments.subList(startIndex, endIndex).mapIndexed { index, (userId, text) ->
            // 원본 인덱스 계산 (desc일 때는 역순이므로 원본 인덱스를 계산해야 함)
            val originalIndex = if (direction == "desc") {
                // desc일 때: sortedSegments는 역순이므로 원본 인덱스는 (size - 1 - (startIndex + index))
                DUMMY_SEGMENTS.size - 1 - (startIndex + index)
            } else {
                // asc일 때: 그대로
                startIndex + index
            }
            
            // 현재 시간 기준으로 과거 시간 생성 (회의 시작 후 시간 경과)
            // 첫 번째 세그먼트는 10초 후, 이후는 20-30초 간격
            val secondsAgo = (DUMMY_SEGMENTS.size - originalIndex) * 25L + 10L
            val generatedTime = LocalDateTime.now(ZoneId.of("Asia/Seoul"))
                .minusSeconds(secondsAgo)
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

            Segment(
                id = meetingId * 10000L + originalIndex, // 고유 ID 생성
                userId = userId,
                segmentOrder = originalIndex,
                timestamp = generatedTime,
                text = text,
                lang = "ko" // 한국어
            )
        }

        return ApiResult.Success(segments)
    }
    
    override suspend fun getTotalSegmentCount(meetingId: Long): ApiResult<Long?> {
        // 더미 데이터의 총 개수 반환
        return ApiResult.Success(DUMMY_SEGMENTS.size.toLong())
    }
}
