package com.imhungry.jjongseol.data.network

import okhttp3.*
import okio.ByteString
import java.util.concurrent.TimeUnit

class WebSocketManager {

    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .readTimeout(3, TimeUnit.SECONDS)
        .build()

    fun connect(url: String, onMessage: (String) -> Unit, onFailure: (Throwable) -> Unit) {
        val request = Request.Builder().url(url).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                onMessage(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                onFailure(t)
            }
        })
    }

    fun sendBinary(data: ByteArray) {
        webSocket?.send(ByteString.of(*data))
    }

    fun close() {
        webSocket?.close(1000, "Closing normally")
    }
}
