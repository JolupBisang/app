package com.imhungry.sillok.presentation.service

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.imhungry.sillok.data.model.realtime.LiveFeedbackDto
import com.imhungry.sillok.data.model.realtime.LiveSummaryDto
import com.imhungry.sillok.data.model.realtime.RealtimeSegmentDto
import com.imhungry.sillok.presentation.util.DateTimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * 서버, Foreground Service, WebSocket, SSE 없이
 * MeetingInProgressViewModel 에 ServiceEvent 를 흘려보내는 Fake 구현.
 *
 * - debug 빌드에서만 컴파일됨 (src/debug 아래라서)
 * - 1초마다 DiarizedSegment, 몇 초마다 Feedback / Summary / ParticipationRate 를 흘려보내서
 *   UI 가 실시간으로 잘 동작하는지 확인할 수 있다.
 */
@Singleton
class FakeMeetingRealtimeEventSource @Inject constructor(
) : MeetingRealtimeEventSource {

    companion object {
        private const val TAG = "FakeMeetingRealtimeES"
    }

    // ViewModel 에서 구독하는 Flow
    private val _events = MutableSharedFlow<ServiceEvent>(
        extraBufferCapacity = 64
    )
    override val events: Flow<ServiceEvent> = _events.asSharedFlow()

    // 내부 코루틴 스코프
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // 이벤트 생성 Job
    private var generatorJob: Job? = null

    // 마이크 on/off 상태 (UI 에서 toggleMic 호출 시 변경)
    private var micEnabled: Boolean = true

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun start(serverUrl: String, meetingId: Long, jwtToken: String) {
        Log.d(TAG, "[start] Fake 이벤트 소스 시작: meetingId=$meetingId")

        // 기존 Job 있으면 취소
        generatorJob?.cancel()

        // 새로 시작
        generatorJob = scope.launch {
            val startTimeIso = DateTimeUtils.getCurrentTime()
            Log.d(TAG, "[start] 가짜 ConnectionEstablished 발행: actualStartTime=$startTimeIso")

            // 1) 연결 확립 이벤트 한 번 발행
            _events.emit(
                ServiceEvent.ConnectionEstablished(
                    actualStartTime = startTimeIso
                )
            )

            // 더미 대화 내용
            val sampleTexts = listOf(
                "오늘 회의 목표는 앱 출시 일정 정리입니다.",
                "UI 쪽은 이번 주까지 시안 확정하는 걸로 할게요.",
                "네",
                "백엔드는 로그인과 회의 생성 API가 우선입니다.",
                "실시간 자막 기능은 다음 스프린트에서 논의하죠.",
                "지금까지 진행 상황 다시 한 번 정리해볼게요."
            )

            // 더미 요약 내용
            val sampleSummaries = listOf(
                "회의 초반에 전체 일정과 역할을 합의했습니다.",
                "실시간 회의록 자동 생성 기능에 대한 요구사항을 정리했습니다.",
                "다음 회의 전까지 각자 액션 아이템을 수행하기로 했습니다."
            )

            var second = 0
            var segmentOrder = 1

            // 2) 3분(180초) 동안 가짜 이벤트 쏴주기
            while (isActive && second <= 185) {
                delay(1000L)
                second++

                // (1) 1초마다 DIARIZED_SEGMENT
                val text = sampleTexts[second % sampleTexts.size]
                val userId = ((second % 4) + 1).toLong() // 1L, 2L, 3L, 4L 순환
                val nowIso = DateTimeUtils.getCurrentTime()

                val segment = RealtimeSegmentDto(
                    userId = userId,
                    order = segmentOrder++,
                    text = text,
                    timestamp = nowIso
                )
                _events.emit(ServiceEvent.DiarizedSegment(segment))

                // (2) 5초마다 참여율 업데이트
                if (second % 5 == 0) {
                    val rates = mapOf(
                        1L to 0.4 + Random.nextDouble(-0.05, 0.05),
                        2L to 0.3 + Random.nextDouble(-0.05, 0.05),
                        3L to 0.2 + Random.nextDouble(-0.03, 0.03),
                        4L to 0.1 + Random.nextDouble(-0.02, 0.02)
                    )
                    _events.emit(ServiceEvent.ParticipationRate(rates))
                }

                // (3) 10초마다 피드백
                if (second % 10 == 0) {
                    val feedbackComment = when (second % 20) {
                        0 -> "속도 좋습니다! 지금 페이스 유지해 주세요."
                        10 -> "조금 벗어난 논의가 있어요. 다시 아젠다로 돌아가 볼까요?"
                        else -> "진행이 안정적으로 잘 되고 있어요."
                    }
                    val feedback = LiveFeedbackDto(
                        comment = feedbackComment
                    )
                    _events.emit(ServiceEvent.Feedback(feedback))
                }

                // (4) 15초마다 요약
                if (second % 15 == 0) {
                    val summaryText = sampleSummaries[(second / 15) % sampleSummaries.size]
                    val summary = LiveSummaryDto(
                        summary = summaryText
                    )
                    _events.emit(ServiceEvent.Summary(summary))
                }

                // (5) 3분(180초) 시점에 COMPLETION_SCHEDULED
                if (second == 180) {
                    Log.d(TAG, "[Fake] COMPLETION_SCHEDULED 발행 (3분)")
                    _events.emit(ServiceEvent.CompletionScheduled)
                }

                // (6) 3분 5초(185초) 시점에 MEETING_COMPLETED
                if (second == 185) {
                    Log.d(TAG, "[Fake] MEETING_COMPLETED 발행 (3분 5초)")
                    _events.emit(
                        ServiceEvent.MeetingCompleted(
                            message = "가짜 회의가 정상적으로 종료되었습니다."
                        )
                    )
                    break
                }
            }

            Log.d(TAG, "[start] Fake 이벤트 소스 종료")
        }
    }

    override suspend fun stop() {
        Log.d(TAG, "[stop] Fake 이벤트 소스 중지")
        generatorJob?.cancel()
        generatorJob = null
    }

    override fun toggleMic() {
        micEnabled = !micEnabled
        Log.d(TAG, "[toggleMic] (fake) micEnabled=$micEnabled")
    }
}
