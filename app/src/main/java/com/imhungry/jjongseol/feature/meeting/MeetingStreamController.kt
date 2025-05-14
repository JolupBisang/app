package com.imhungry.jjongseol.feature.meeting

import android.content.Context
import android.util.Log
import com.imhungry.jjongseol.data.network.client.WebSocketManager
import com.imhungry.jjongseol.feature.audio.StreamingController
import com.imhungry.jjongseol.feature.audio.devtool.TestDataSender
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class MeetingStreamController @Inject constructor(
    private val streamingController: StreamingController,
    private val testDataSender: TestDataSender,
    private val webSocketManager: WebSocketManager,
    @ApplicationContext private val context: Context
) {
    private val _micEnabled = MutableStateFlow(true)
    val micEnabled: StateFlow<Boolean> = _micEnabled.asStateFlow()
    var onWebSocketErrorMessage: ((String) -> Unit)? = null

    fun startStreamingSafely(meetingId: Long, jwtToken: String): Boolean {
        Log.d("MeetingStreamCtrl", "startStreamingSafely() 호출됨. meetingId=$meetingId")

        if (!streamingController.startStreamingService()) {
            Log.e("MeetingStreamCtrl", "StreamingService 실행 실패: 권한 없음")
            return false
        }

        Log.d("MeetingStreamCtrl", "StreamingService 실행 성공 → WebSocket 연결 시도")
        webSocketManager.connect(
            meetingId = meetingId,
            jwtToken = jwtToken,
            onMessage = { Log.d("WebSocket", "서버 메시지 수신: $it") },
            onFailure = { throwable -> Log.e("WebSocket", "WebSocket 실패", throwable) },
            onErrorMessage = { message ->
                Log.e("WebSocket", "WebSocket 에러 메시지 수신: $message")
                onWebSocketErrorMessage?.invoke(message)
            }
        )

        return true
    }

    fun stopStreaming() {
        webSocketManager.close()
        streamingController.stopStreamingService()
    }

    fun pauseEncoding() = streamingController.pauseEncoding()
    fun resumeEncoding() = streamingController.resumeEncoding()

    fun toggleMic(enabled: Boolean) {
        _micEnabled.value = enabled
        if (enabled) resumeEncoding() else pauseEncoding()
    }

    fun resetMicState() {
        _micEnabled.value = true
    }

    fun startSendingTestData(meetingId: Long, scope: CoroutineScope) {
        testDataSender.startSummary(meetingId, scope)
        testDataSender.startParticipation(meetingId, scope)
        testDataSender.startFeedback(meetingId, scope)
    }

    fun stopSendingTestData() {
        testDataSender.stopSummary()
        testDataSender.stopParticipation()
        testDataSender.stopFeedback()
    }
}
