// 회의 단위로 SSE와 WebSocket을 오케스트레이션하고 재연결을 관리하는 클라이언트
package com.imhungry.sillok.data.remote.realtime

import android.util.Log
import com.imhungry.sillok.data.local.TokenStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import okhttp3.OkHttpClient
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/**
 * RealtimeClient는 하나의 회의에 대해 실시간 통신(SSE, WebSocket)을 오케스트레이션합니다.
 *
 * 역할
 * - SSE(`SseClient`)와 WebSocket(`WebSocketClient`)을 함께 시작하고 상태를 감시합니다.
 * - 수신한 모든 실시간 이벤트를 내부 SharedFlow와 전역 `RealtimeEventBus`로 전달합니다.
 * - 제공된 코루틴 스코프가 살아있는 동안 지수 백오프 기반 재연결 루프를 수행합니다.
 *
 * 동작
 * - 두 전송(SSE/WS)은 동시에 시작되며, 둘 중 하나라도 실패하면 둘 다 정리 후 재시도합니다.
 * - 재시도 대기시간은 최대 `MAX_BACKOFF_MS`까지 증가합니다.
 */
class RealtimeClient(
    private val okHttpClient: OkHttpClient,
    private val sseOkHttpClient: OkHttpClient,
    private val tokenStore: TokenStore,
    private val eventBus: RealtimeEventBus,
    private val packetStore: AudioPacketStore,
    private val scope: CoroutineScope,
) {
    // 내부 전용 이벤트 스트림으로, 구독 UI가 없을 때도 버퍼링을 허용합니다.
    private val _events = MutableSharedFlow<RealtimeEvent>(extraBufferCapacity = 64)
    // 외부 공개 읽기 전용 스트림
    val events: SharedFlow<RealtimeEvent> = _events
    // 재연결 루프 동시 실행을 방지하는 락 플래그입니다.
    private val reconnectJobLock = AtomicBoolean(false)
    // 회의가 정상 종료되었는지 표시합니다.
    private val terminated = AtomicBoolean(false)
    // 백그라운드에서 재연결을 수행하는 잡 핸들입니다.
    private var reconnectJob: Job? = null
    // SSE 클라이언트: 이벤트를 수신하면 내부/전역 스트림으로 전달합니다.
    private val sseClient: SseClient = SseClient(sseOkHttpClient) { ev ->
        _events.tryEmit(ev)
        eventBus.tryEmit(ev)
        if (ev is RealtimeEvent.MeetingCompleted) {
            terminated.set(true)
            runCatching { wsClient.stop() }
            runCatching { sseClient.stop() }
        }
    }
    // WS 클라이언트: 오디오 업스트림과 서버 이벤트를 처리합니다.
    private val wsClient: WebSocketClient = WebSocketClient(okHttpClient, packetStore, scope) { ev ->
        _events.tryEmit(ev)
        eventBus.tryEmit(ev)
        if (ev is RealtimeEvent.MeetingCompleted) {
            terminated.set(true)
            runCatching { wsClient.stop() }
            runCatching { sseClient.stop() }
        }
    }

    /**
     * 지정된 회의에 대한 실시간 세션을 시작합니다.
     *
     * - SSE/WS 클라이언트를 초기화하고 시작합니다.
     * - 둘 중 하나라도 중단되면 둘 다 정리하고 백오프로 재시작합니다.
     */
    fun start(meetingId: Long) {
        // 다중 start 호출을 방어합니다.
        if (reconnectJobLock.getAndSet(true)) return
        reconnectJob?.cancel()
        reconnectJob = scope.launch(Dispatchers.IO) {
            var attempt = 0
            while (isActive) {
                // 현재 액세스 토큰을 동기로 조회합니다.
                val token = runBlocking { tokenStore.accessToken.first() }
                try {
                    // SSE/WS를 동시에 시작합니다.
                    sseClient.start(meetingId, token)
                    wsClient.start(meetingId, token)
                    attempt = 0
                    // 두 연결이 모두 살아있는 동안 주기적으로 상태를 확인합니다.
                    while (isActive && sseClient.isAlive() && wsClient.isAlive()) {
                        delay(5_000)
                    }
                } catch (t: Throwable) {
                    Log.e(TAG, "Realtime connect error", t)
                } finally {
                    // 종료/오류 시 자원을 정리합니다.
                    sseClient.stop()
                    wsClient.stop()
                }
                if (terminated.get()) break
                attempt += 1
                // 지수 백오프(최대 60초)로 재시도합니다.
                val backoffMs = (1_000L * (1 shl minOf(attempt, 6))).coerceAtMost(MAX_BACKOFF_MS)
                delay(backoffMs)
            }
        }
    }

    /**
     * 실시간 세션을 중지하고 재연결 루프를 종료합니다.
     */
    fun stop() {
        reconnectJob?.cancel()
        reconnectJob = null
        sseClient.stop()
        wsClient.stop()
        reconnectJobLock.set(false)
        terminated.set(true)
    }

    companion object {
        private const val TAG = "RealtimeClient"
        private const val MAX_BACKOFF_MS = 60_000L
    }
}

