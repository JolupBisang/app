package com.imhungry.jjongseol.feature.meeting

import android.content.Context
import android.util.Log
import com.imhungry.jjongseol.data.network.client.WebSocketManager
import com.imhungry.jjongseol.feature.audio.StreamingController
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class MeetingStreamController @Inject constructor(
    private val streamingController: StreamingController,
    private val webSocketManager: WebSocketManager,
    @ApplicationContext private val context: Context
) {
    private val _micEnabled = MutableStateFlow(true)
    val micEnabled: StateFlow<Boolean> = _micEnabled.asStateFlow()

    var onWebSocketErrorMessage: ((String) -> Unit)? = null

    fun startStreamingSafely(meetingId: Long, jwtToken: String): Boolean {
        if (!streamingController.startStreamingService(meetingId, jwtToken)) {
            Log.e("MeetingStreamCtrl", "StreamingService 실행 실패: 권한 없음")
            return false
        }
        return true
    }

    fun stopStreaming(deleteLocalPackets: Boolean = false) {
        webSocketManager.close()
        streamingController.stopStreamingService(deleteLocalPackets)
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
}
