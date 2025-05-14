package com.imhungry.jjongseol.data.network.client

import android.util.Log
import com.google.gson.Gson
import com.imhungry.jjongseol.BuildConfig
import com.imhungry.jjongseol.data.model.error.ApiError
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class WebSocketManager @Inject constructor() {
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .readTimeout(3, TimeUnit.SECONDS)
        .build()

    fun connect(
        meetingId: Long,
        jwtToken: String,
        onMessage: (String) -> Unit,
        onFailure: (Throwable) -> Unit,
        onErrorMessage: (String) -> Unit
    ) {
        val url = "ws://${BuildConfig.IP_ADDRESS}:8080/ws/meeting/audio/$meetingId?token=$jwtToken"

        val request = Request.Builder()
            .url(url)
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("Audio", "onMessage: $text")
                try {
                    val apiError = Gson().fromJson(text, ApiError::class.java)
                    apiError.message?.let {
                        Log.w("Audio", "ApiError 감지: $it")
                        onErrorMessage(it)
                    }
                } catch (e: Exception) {
                    Log.d("Audio", "일반 텍스트 메시지 수신: $text")
                }
                onMessage(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("Audio", "WebSocket 연결 실패", t)
                onFailure(t)
            }
        })
    }

    fun sendBinary(data: ByteArray) {
        val success = webSocket?.send(ByteString.of(*data)) ?: false
        if (!success) {
            // 전송 실패 시 처리
        }
    }

    fun close() {
        webSocket?.close(1000, "Normal closure")
        webSocket = null
    }
}
