package com.imhungry.sillok.presentation.service

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.imhungry.sillok.data.model.realtime.AgendaStatusChangedMessage
import com.imhungry.sillok.data.model.realtime.ErrorResponse
import com.imhungry.sillok.data.model.realtime.RealtimeSegmentDto
import com.imhungry.sillok.data.model.realtime.SocketResponse
import com.imhungry.sillok.data.model.realtime.SocketResponseType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request.Builder
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

/**
 * WebSocket 연결 및 메시지 처리를 담당하는 클래스
 */
@RequiresApi(Build.VERSION_CODES.O)
class WebSocketManager(
    private val serviceScope: CoroutineScope,
    private val serviceEvents: MutableSharedFlow<ServiceEvent>,
    private val onConnectionEstablished: (Long?, WebSocket) -> Unit,
    private val onMeetingCompleted: () -> Unit = {}
) {
    companion object {
        private const val TAG = "WebSocketManager"
    }

    private val gson = Gson()
    private var webSocket: WebSocket? = null

    private val okHttpClient = OkHttpClient.Builder()
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()

    fun connect(serverUrl: String, meetingId: Long, jwtToken: String) {
        serviceScope.launch {
            try {
                Log.d(TAG, "[WebSocket-1] WebSocket 연결 시작")
                val wsUrl = "${serverUrl}ws/v1/meeting/$meetingId/audio"
                Log.d(TAG, "[WebSocket-1-1] WebSocket URL: $wsUrl")

                val request = Builder()
                    .url(wsUrl)
                    .addHeader("Authorization", "Bearer $jwtToken")
                    .build()

                Log.d(TAG, "[WebSocket-1-2] WebSocket 요청 생성 완료")
                webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        Log.d(TAG, "========================================")
                        Log.d(TAG, "[WebSocket-2] WebSocket 연결 성공!")
                        Log.d(TAG, "  - Response Code: ${response.code}")
                        Log.d(TAG, "========================================")
                    }

                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onMessage(webSocket: WebSocket, text: String) {
                        Log.d(TAG, "[WebSocket-3] WebSocket 메시지 수신: ${text.take(100)}...")
                        parseWebSocketMessage(text, webSocket)
                    }

                    override fun onFailure(
                        webSocket: WebSocket,
                        t: Throwable,
                        response: Response?
                    ) {
                        Log.e(TAG, "[WebSocket-실패] WebSocket 연결 실패: ${t.message}", t)
                        Log.e(TAG, "  - Response: ${response?.code}")
                    }
                })
                Log.d(TAG, "[WebSocket-1-3] WebSocket 리스너 등록 완료")
            } catch (e: Exception) {
                Log.e(TAG, "[WebSocket-예외] WebSocket 연결 예외: ${e.message}", e)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun parseWebSocketMessage(jsonString: String, webSocket: WebSocket) {
        try {
            val jsonObject = JsonParser.parseString(jsonString).asJsonObject
            val typeString = jsonObject.get("type")?.asString
            val socketType = typeString.toSocketResponseType()

            when (socketType) {
                SocketResponseType.CONNECTION_ESTABLISHED -> {
                    val response = gson.fromJson<SocketResponse<Long>>(
                        jsonString,
                        object : TypeToken<SocketResponse<Long>>() {}.type
                    )
                    onConnectionEstablished(response.data, webSocket)
                }

                SocketResponseType.DIARIZED_SEGMENT -> {
                    val response = gson.fromJson<SocketResponse<RealtimeSegmentDto>>(
                        jsonString,
                        object : TypeToken<SocketResponse<RealtimeSegmentDto>>() {}.type
                    )
                    response.data?.let { segment ->
                        serviceScope.launch {
                            serviceEvents.emit(ServiceEvent.DiarizedSegment(segment))
                        }
                    }
                }

                SocketResponseType.COMPLETION_SCHEDULED -> {
                    // 녹음과 SSE 연결 해제 (Service는 WebSocket 연결 유지하여 회의록 생성 완료 기다림)
                    onMeetingCompleted()
                    serviceScope.launch {
                        serviceEvents.emit(ServiceEvent.CompletionScheduled)
                    }
                }

                SocketResponseType.MEETING_COMPLETED -> {
                    val response = gson.fromJson<SocketResponse<String>>(
                        jsonString,
                        object : TypeToken<SocketResponse<String>>() {}.type
                    )
                    serviceScope.launch {
                        serviceEvents.emit(ServiceEvent.MeetingCompleted(response.data))
                    }
                }

                SocketResponseType.ERROR -> {
                    val response = gson.fromJson<SocketResponse<ErrorResponse>>(
                        jsonString,
                        object : TypeToken<SocketResponse<ErrorResponse>>() {}.type
                    )
                    serviceScope.launch {
                        serviceEvents.emit(ServiceEvent.Error(response.data))
                    }
                }

                SocketResponseType.AGENDA_UPDATED -> {
                    val response = gson.fromJson<SocketResponse<AgendaStatusChangedMessage>>(
                        jsonString,
                        object : TypeToken<SocketResponse<AgendaStatusChangedMessage>>() {}.type
                    )
                    response.data?.let { data ->
                        serviceScope.launch {
                            serviceEvents.emit(
                                ServiceEvent.AgendaUpdated(
                                    data.agendaId,
                                    data.isCompleted
                                )
                            )
                        }
                    } ?: run {
                        Log.w(TAG, "AGENDA_UPDATED 데이터가 null입니다")
                    }
                }

                else -> {
                    Log.w(TAG, "알 수 없는 메시지 타입: $typeString")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "메시지 파싱 실패: ${e.message}", e)
        }
    }

    fun getWebSocket(): WebSocket? = webSocket

    fun close() {
        webSocket?.close(1000, "서비스 종료")
        webSocket = null
    }

    private fun String?.toSocketResponseType(): SocketResponseType? {
        return try {
            SocketResponseType.valueOf(this ?: "")
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}

