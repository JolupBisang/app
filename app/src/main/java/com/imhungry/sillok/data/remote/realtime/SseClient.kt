// 요약/피드백/참여도 SSE 채널을 구독해 RealtimeEvent로 변환하는 클라이언트
package com.imhungry.sillok.data.remote.realtime

import android.util.Log
import com.google.gson.Gson
import com.imhungry.sillok.BuildConfig
import okhttp3.Request
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import okhttp3.OkHttpClient

/**
 * SseClient는 회의 단위의 Server-Sent Events 구독을 관리합니다.
 *
 * 역할
 * - SUMMARY, FEEDBACK, PARTICIPATION_RATE 채널에 SSE 연결을 엽니다.
 * - 수신한 데이터를 파싱해 `RealtimeEvent`로 변환해 전달합니다.
 * - 채널별 연결 상태를 추적하고, 전체 생존 여부를 `isAlive()`로 제공합니다.
 *
 * 참고
 * - 토큰이 제공되면 Authorization 헤더가 자동으로 추가됩니다.
 */
class SseClient(
    private val sseOkHttpClient: OkHttpClient,
    private val emit: (RealtimeEvent) -> Unit,
) : RealtimeConnection {

    private val gson = Gson()
    // 채널별 EventSource 핸들을 관리합니다(열림 여부 확인/종료 처리용).
    private val sseByChannel: MutableMap<SseChannel, EventSource?> = mutableMapOf(
        SseChannel.SUMMARY to null,
        SseChannel.FEEDBACK to null,
        SseChannel.PARTICIPATION_RATE to null,
    )
// /api/v1/meetings/{meetingId}/events/subscribe
    /** 지정된 회의에 대한 모든 SSE 채널을 시작합니다. */
    override fun start(meetingId: Long, token: String?) {
        // 베이스 URL 뒤 슬래시 중복을 피하기 위해 트림합니다.
        val baseUrl = BuildConfig.BASE_URL.trimEnd('/')
        connectSse(
            channel = SseChannel.SUMMARY,
            url = "$baseUrl/api/sse/subscribe/summary/$meetingId",
            token = token,
        )
        connectSse(
            channel = SseChannel.FEEDBACK,
            url = "$baseUrl/api/sse/subscribe/feedback/$meetingId",
            token = token,
        )
        connectSse(
            channel = SseChannel.PARTICIPATION_RATE,
            url = "$baseUrl/api/sse/subscribe/participation-rate/$meetingId",
            token = token,
        )
    }

    /** 모든 SSE 연결을 종료합니다. */
    override fun stop() {
        for (channel in sseByChannel.keys) {
            // EventSource.cancel()은 네트워크 자원을 즉시 해제합니다.
            try { sseByChannel[channel]?.cancel() } catch (_: Throwable) {}
            sseByChannel[channel] = null
        }
    }

    /** 모든 SSE 채널이 열린 상태면 true를 반환합니다. */
    fun isAlive(): Boolean {
        return sseByChannel.values.all { it != null }
    }

    private fun connectSse(channel: SseChannel, url: String, token: String?) {
        val requestBuilder = Request.Builder().url(url)
        if (!token.isNullOrBlank()) {
            // 인증 토큰이 있으면 Authorization 헤더를 추가합니다.
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }
        val request = requestBuilder.build()
        val listener = object : EventSourceListener() {
            override fun onOpen(eventSource: EventSource, response: Response) {
                sseByChannel[channel] = eventSource
                Log.d(TAG, "SSE($channel) open: ${response.code}")
            }

            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                val eventType = parseEventType(type)
                handleSseEvent(channel, eventType, data)
            }

            override fun onClosed(eventSource: EventSource) {
                Log.d(TAG, "SSE($channel) closed")
                sseByChannel[channel] = null
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                Log.e(TAG, "SSE($channel) failure", t)
                sseByChannel[channel] = null
            }
        }
        // OkHttp SSE 팩토리로 EventSource를 생성하고 즉시 연결합니다.
        sseByChannel[channel] = EventSources.createFactory(sseOkHttpClient).newEventSource(request, listener)
    }

    private fun parseEventType(type: String?): MeetingSseEventType {
        return when (type?.uppercase()) {
            "CONNECT" -> MeetingSseEventType.CONNECT
            "SUMMARY" -> MeetingSseEventType.SUMMARY
            "FEEDBACK" -> MeetingSseEventType.FEEDBACK
            "PARTICIPATION_RATE" -> MeetingSseEventType.PARTICIPATION_RATE
            else -> MeetingSseEventType.UNKNOWN
        }
    }

    private fun handleSseEvent(channel: SseChannel, eventType: MeetingSseEventType, data: String) {
        when (eventType) {
            MeetingSseEventType.CONNECT -> {
                Log.d(TAG, "SSE($channel) CONNECT $data")
            }
            MeetingSseEventType.SUMMARY -> {
                try {
                    val summary = parseSummary(data)
                    emit(RealtimeEvent.Summary(timestamp = summary.timestamp, summary = summary.summary))
                } catch (t: Throwable) {
                    Log.e(TAG, "SSE($channel) SUMMARY parse error", t)
                }
            }
            MeetingSseEventType.FEEDBACK -> {
                try {
                    val feedback = parseFeedback(data)
                    emit(RealtimeEvent.Feedback(timestamp = feedback.timestamp, comment = feedback.comment))
                } catch (t: Throwable) {
                    Log.e(TAG, "SSE($channel) FEEDBACK parse error", t)
                }
            }
            MeetingSseEventType.PARTICIPATION_RATE -> {
                try {
                    val pr = parseParticipationRates(data)
                    emit(
                        RealtimeEvent.ParticipationRate(
                            timestamp = pr.timestamp,
                            participationRates = pr.participationRates.map { RealtimeEvent.ParticipationRate.Item(it.userId, it.rate) }
                        )
                    )
                } catch (t: Throwable) {
                    Log.e(TAG, "SSE($channel) PARTICIPATION_RATE parse error", t)
                }
            }
            MeetingSseEventType.UNKNOWN -> {
                Log.d(TAG, "SSE($channel) UNKNOWN type data=$data")
            }
        }
    }

    private fun parseSummary(data: String): SummaryEvent = gson.fromJson(data, SummaryEvent::class.java)

    private fun parseFeedback(data: String): FeedbackEvent = gson.fromJson(data, FeedbackEvent::class.java)

    private fun parseParticipationRates(data: String): ParticipationRateEvent {
        val payload = gson.fromJson(data, ParticipationRatePayload::class.java)
        val list = payload.participationRates.orEmpty().filter { it.userId != -1L }
        return ParticipationRateEvent(timestamp = payload.timestamp.orEmpty(), participationRates = list)
    }

    private data class SummaryEvent(
        val timestamp: String,
        val summary: String,
    )

    private data class FeedbackEvent(
        val timestamp: String,
        val comment: String,
    )

    private data class ParticipationRate(
        val userId: Long,
        val rate: Double,
    )

    private data class ParticipationRateEvent(
        val timestamp: String,
        val participationRates: List<ParticipationRate>,
    )

    private data class ParticipationRatePayload(
        val timestamp: String?,
        val participationRates: List<ParticipationRate>?
    )

    companion object {
        private const val TAG = "SseClient"
    }
}

enum class MeetingSseEventType {
    CONNECT,
    SUMMARY,
    FEEDBACK,
    PARTICIPATION_RATE,
    UNKNOWN,
}

enum class SseChannel {
    SUMMARY,
    FEEDBACK,
    PARTICIPATION_RATE,
}

interface RealtimeConnection {
    fun start(meetingId: Long, token: String?)
    fun stop()
}


