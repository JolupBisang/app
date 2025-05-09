package com.imhungry.jjongseol.data.network

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.imhungry.jjongseol.BuildConfig
import okhttp3.*
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources

class SseClient(
    private val client: OkHttpClient,
    private val cookieProvider: () -> String?
) {
    private var eventSource: EventSource? = null
    private var isManuallyClosed = false

    fun subscribeToEvent(
        endpoint: String,
        meetingId: Long,
        eventType: String,
        onEventReceived: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        isManuallyClosed = false

        val request = Request.Builder()
            .url(BuildConfig.BASE_URL + "api/$endpoint/subscribe/$meetingId")
            .header("Authorization", "Bearer ${cookieProvider()?.removePrefix("Bearer ")}")
            .build()

        val factory = EventSources.createFactory(client)
        eventSource = factory.newEventSource(request, object : EventSourceListener() {
            override fun onOpen(eventSource: EventSource, response: Response) {
                Log.d("SSE", "$eventType 연결 성공")
            }

            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                Log.d("SSE", "$eventType 이벤트 수신: $type, data=$data")
                if (type == eventType) {
                    onEventReceived(data)
                }
            }

            override fun onClosed(eventSource: EventSource) {
                Log.d("SSE", "$eventType 연결 종료")
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                Log.e("SSE", "$eventType 연결 실패: ${t?.message}")
                if (!isManuallyClosed) {
                    onError(t?.message ?: "알 수 없는 오류")
                    reconnect(meetingId, eventType, onEventReceived, onError)
                }
            }
        })
    }

    fun disconnect() {
        isManuallyClosed = true
        eventSource?.cancel()
        eventSource = null
        Log.d("SSE", "SSE 연결 해제")
    }

    private fun reconnect(
        meetingId: Long,
        eventType: String,
        onEventReceived: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        Handler(Looper.getMainLooper()).postDelayed({
            Log.d("SSE", "$eventType SSE 재연결 시도 중...")
            subscribeToEvent(eventType.lowercase(), meetingId, eventType, onEventReceived, onError)
        }, 3000)
    }
}
