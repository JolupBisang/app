package com.imhungry.jjongseol.data.network.client

import android.util.Log
import com.google.gson.Gson
import com.imhungry.jjongseol.BuildConfig
import com.imhungry.jjongseol.data.model.error.ApiError
import com.imhungry.jjongseol.data.model.response.SocketResponse
import com.imhungry.jjongseol.data.model.response.SocketResponseType
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

    private var isConnected = false

    fun connect(
        meetingId: Long,
        jwtToken: String,
        onMessage: (String) -> Unit,
        onFailure: (Throwable) -> Unit,
        onErrorMessage: (String) -> Unit,
        onChunkIdReceived: ((Int) -> Unit)? = null,
    ) {
        if (isConnected) {
            Log.w("WebSocket", "이미 연결되어 있음. 기존 연결 종료 후 재연결")
            close()
        }

        val url = "ws://${BuildConfig.IP_ADDRESS}:8080/ws/meeting/audio/$meetingId?token=$jwtToken"
        val request = Request.Builder().url(url).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                isConnected = true
                Log.i("WebSocket", "WebSocket 연결 성공 (code=${response.code})")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WebSocket", "onMessage 수신됨: $text")

                try {
                    val response = Gson().fromJson(text, SocketResponse::class.java)
                    when (response.type) {
                        SocketResponseType.LAST_PROCESSED_CHUNK_ID -> {
                            val lastChunkId = (response.data as Double).toInt()
                            Log.i("WebSocket", "마지막 chunkId 수신됨: $lastChunkId")
                            onChunkIdReceived?.invoke(lastChunkId)
                        }
                        SocketResponseType.ERROR -> {
                            val error = Gson().fromJson(Gson().toJson(response.data), ApiError::class.java)
                            val message = error.message ?: "알 수 없는 오류가 발생했습니다"
                            Log.w("WebSocket", "WebSocket 에러 메시지 수신: $message")
                            onErrorMessage(message)
                        }
                        else -> Log.d("WebSocket", "알 수 없는 메시지 타입 수신: ${response.type}")
                    }
                } catch (e: Exception) {
                    Log.w("WebSocket", "SocketResponse 파싱 실패. 일반 텍스트로 처리", e)
                    onMessage(text)
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                isConnected = false
                Log.e("WebSocket", "연결 실패 (responseCode=${response?.code})", t)
                onFailure(t)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                isConnected = false
                Log.i("WebSocket", "연결 종료됨: code=$code, reason=$reason")
            }
        })
    }

    fun sendBinary(data: ByteArray) {
        val success = webSocket?.send(ByteString.of(*data)) ?: false
        if (!success) {
            Log.e("WebSocket", "바이너리 전송 실패 - 연결 상태 확인 필요")
        }
    }

    fun close() {
        if (isConnected) {
            webSocket?.close(1000, "Normal closure")
            webSocket = null
            isConnected = false
        }
    }
}
