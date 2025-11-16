package com.imhungry.sillok.data.repository.summary

import android.os.Build
import androidx.annotation.RequiresApi
import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.model.summary.Summary
import com.imhungry.sillok.domain.repository.summary.SummaryRepository
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * debug 전용 SummaryRepository.
 *
 * 회의 진행 중 요약 리스트를 시뮬레이션합니다.
 * - isRecap=false: 중간 요약 (회의 진행 중 생성)
 * - isRecap=true: 최종 요약 (회의 종료 후 생성)
 */
@Singleton
@RequiresApi(Build.VERSION_CODES.O)
class FakeSummaryRepository @Inject constructor() : SummaryRepository {

    companion object {
        // 중간 요약 더미 데이터
        private val INTERIM_SUMMARY_CONTENTS = listOf(
            "회의 목적과 범위를 합의함",
            "핵심 액션 아이템 3개 도출했고 길게 작성했을 때는 이런 모습이고, 가로 너비는 고정되어 있으니 아래로 길어짐.",
            "프로젝트 일정과 마일스톤 논의 완료",
            "담당자 배정 및 역할 분담 결정",
            "다음 회의까지 완료해야 할 작업 항목 정리",
            "예산 및 리소스 할당에 대한 합의",
            "기술 스택 선택 및 아키텍처 방향성 확정",
            "디자인 시스템 구축 방안 논의",
            "사용자 피드백 반영 계획 수립",
            "품질 관리 및 테스트 전략 수립"
        )

        // 최종 요약 더미 데이터
        private val RECAP_SUMMARY_CONTENTS = listOf(
            """
                프로젝트 초기 기획 단계에서 목표 설정, 일정 수립, 역할 분담이 완료되었습니다.
                기술 스택은 React와 Node.js로 확정되었으며, 디자인 시스템은 Material Design 기반으로 구축하기로 결정했습니다.
                다음 주까지 각 팀원은 담당 업무의 상세 계획서를 작성하여 공유하기로 했습니다.
                다음 회의는 2024년 1월 15일 오후 2시에 진행 예정이며, 각 팀의 진행 상황을 공유하는 시간을 가질 예정입니다.
            """.trimIndent()
        )
    }

    override suspend fun getSummaries(
        meetingId: Long,
        isRecap: Boolean,
        page: Int,
        size: Int
    ): ApiResult<List<Summary>> {
        val contents = if (isRecap) {
            RECAP_SUMMARY_CONTENTS
        } else {
            INTERIM_SUMMARY_CONTENTS
        }

        // 페이지네이션 처리
        val startIndex = page * size
        val endIndex = minOf(startIndex + size, contents.size)
        
        if (startIndex >= contents.size) {
            return ApiResult.Success(emptyList())
        }

        val summaries = contents.subList(startIndex, endIndex).mapIndexed { index, content ->
            val summaryIndex = startIndex + index
            // 현재 시간 기준으로 과거 시간 생성 (최신 요약이 먼저 오도록)
            val minutesAgo = (contents.size - summaryIndex) * 5L // 5분 간격
            val generatedTime = LocalDateTime.now(ZoneId.of("Asia/Seoul"))
                .minusMinutes(minutesAgo)
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

            Summary(
                id = meetingId * 1000L + summaryIndex, // 고유 ID 생성
                content = content,
                isRecap = isRecap,
                generatedDateTime = generatedTime
            )
        }

        return ApiResult.Success(summaries)
    }
}
