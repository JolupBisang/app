package com.imhungry.jjongseol.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.imhungry.jjongseol.data.audio.RealTimeAudioStreamer
import com.imhungry.jjongseol.data.network.WebSocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MeetingViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val context by lazy { application.applicationContext }

    private var streamer: RealTimeAudioStreamer? = null

    fun startStreaming() {
        val ws = WebSocketManager().apply {
            connect(
                url = "ws://your.server/ws/audio",
                onMessage = { Log.d("WebSocket", "응답: $it") },
                onFailure = { Log.e("WebSocket", "오류", it) }
            )
        }

        streamer = RealTimeAudioStreamer(
            userId = 1L,
            meetingId = 1L,
            webSocketManager = ws,
            cacheDir = context.cacheDir
        )
        streamer?.start(viewModelScope)
    }

    fun stopStreaming() {
        streamer?.stop()
    }
}
