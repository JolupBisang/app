// 마이크 PCM을 패킷화해 전송하고 누락분 재전송을 처리하는 오디오 WebSocket 클라이언트
package com.imhungry.sillok.data.remote.realtime

import android.util.Log
import com.google.gson.Gson
import com.imhungry.sillok.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.AudioFormat
import android.os.Build
import androidx.annotation.RequiresApi
import okio.ByteString
import org.json.JSONObject
import java.nio.ByteBuffer
import java.time.LocalDateTime

/**
 * WebSocketClient는 회의의 오디오 전용 WebSocket을 관리합니다.
 *
 * 역할
 * - ws://<WS_HOST>/ws/meeting/{meetingId}/audio로 연결(토큰이 있으면 Authorization 헤더 포함)
 * - CONNECTION_ESTABLISHED 수신 시: 이벤트 전파 → 로컬 누락 패킷 재전송 → chunkId 초기화 → 실시간 녹음 시작
 * - 마이크 PCM 프레임을 지속적으로 읽어 패킷(메타 JSON 길이 + JSON + PCM)으로 만들고 저장/전송
 * - 소켓 생명주기(open/message/closing/closed/failure) 처리 및 stop 시 오디오 리소스 해제
 *
 * 참고
 * - chunkId는 회의별 단조 증가하며, 재연결 후에는 max(서버, 로컬)+1부터 재개합니다.
 * - 내부 코루틴 스코프를 주입받아 녹음 루프를 실행합니다.
 */
class WebSocketClient(
    private val okHttpClient: OkHttpClient,
    private val packetStore: AudioPacketStore,
    private val scope: CoroutineScope,
    private val emit: (RealtimeEvent) -> Unit,
) : RealtimeConnection {

    private val gson = Gson()
    // 현재 활성화된 웹소켓 세션 핸들입니다.
    private var webSocket: WebSocket? = null
    // 현재 회의 ID를 보관합니다.
    private var currentMeetingId: Long = -1
    // 전송되는 오디오 패킷의 순번입니다(단조 증가).
    private var chunkId: Long = 0
    // 마이크 입력을 위한 AudioRecord 인스턴스입니다.
    private var audioRecord: AudioRecord? = null
    // 녹음-전송 루프를 수행하는 코루틴 잡입니다.
    private var recordJob: Job? = null
    // 스트리밍 활성화 플래그입니다.
    private var isStreaming: Boolean = false
    // 16kHz 모노 PCM 설정입니다.
    private val sampleRate = 16000
    // 1초 분량 프레임(샘플 수)입니다.
    private val frameSize = 16000
    // AudioRecord 최소 버퍼를 계산해 프레임 사이즈 이상으로 보정합니다.
    private val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    ).coerceAtLeast(frameSize)

    override fun start(meetingId: Long, token: String?) {
        currentMeetingId = meetingId
        val url = "ws://${BuildConfig.WS_HOST}/ws/meeting/$meetingId/audio"
        val requestBuilder = Request.Builder().url(url)
        if (!token.isNullOrBlank()) {
            // 인증 토큰이 있으면 Authorization 헤더를 추가합니다.
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }
        val request = requestBuilder.build()
        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                this@WebSocketClient.webSocket = webSocket
                Log.d(TAG, "WebSocket open: ${response.code}")
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val envelope = gson.fromJson(text, SocketEnvelope::class.java)
                    handleSocketMessage(envelope, meetingId)
                } catch (t: Throwable) {
                    Log.e(TAG, "WebSocket message parse error", t)
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closing: $code $reason")
                webSocket.close(code, reason)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closed: $code $reason")
                this@WebSocketClient.webSocket = null
                stopRecording()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WebSocket failure", t)
                this@WebSocketClient.webSocket = null
                stopRecording()
            }
        }
        // 비동기 웹소켓 연결을 시작합니다.
        this.webSocket = okHttpClient.newWebSocket(request, listener)
    }

    override fun stop() {
        // 연결을 즉시 종료하고 리소스를 정리합니다.
        try { webSocket?.cancel() } catch (_: Throwable) {}
        webSocket = null
        stopRecording()
    }

    fun isAlive(): Boolean = webSocket != null

    private fun resendMissingLocalPackets(meetingId: Long, lastServerChunkId: Long) {
        // 서버가 처리했다고 통보한 chunkId 이후의 로컬 패킷을 재전송합니다.
        val lastLocal = packetStore.findLastLocalChunkId(meetingId)
        if (lastLocal <= lastServerChunkId) return
        for (id in (lastServerChunkId + 1)..lastLocal) {
            val bytes = packetStore.readPacket(meetingId, id) ?: break
            val ok = webSocket?.send(ByteString.of(*bytes)) ?: false
            if (!ok) {
                Log.w(TAG, "Failed to resend packet id=$id")
                break
            } else {
                Log.d(TAG, "Resent local packet packet_$id.bin")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startRecording() {
        // 이미 녹음 중이면 중복 시작을 방지합니다.
        if (audioRecord != null && audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) return
        // 권한 거부 가능성에 대비해 AudioRecord 생성 및 시작을 SecurityException으로 명시 처리합니다.
        val recorder = try {
            AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )
        } catch (se: SecurityException) {
            Log.e(TAG, "RECORD_AUDIO permission denied when creating AudioRecord", se)
            emit(RealtimeEvent.SocketError(message = "Microphone permission denied"))
            return
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to create AudioRecord", t)
            emit(RealtimeEvent.SocketError(message = "Failed to create recorder"))
            return
        }
        try {
            recorder.startRecording()
        } catch (se: SecurityException) {
            Log.e(TAG, "RECORD_AUDIO permission denied when starting recording", se)
            emit(RealtimeEvent.SocketError(message = "Microphone permission denied"))
            try { recorder.release() } catch (_: Throwable) {}
            return
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to start recording", t)
            try { recorder.release() } catch (_: Throwable) {}
            emit(RealtimeEvent.SocketError(message = "Failed to start recording"))
            return
        }
        audioRecord = recorder
        isStreaming = true
        recordJob?.cancel()
        recordJob = scope.launch(Dispatchers.IO) {
            // 프레임당 2바이트(16bit) × 프레임 크기만큼 버퍼를 준비합니다.
            val pcmBuffer = ByteArray(frameSize * 2)
            // 음수가 되는 것을 방지합니다.
            if (chunkId < 0L) chunkId = 0L
            while (isActive && isStreaming) {
                val read = try { audioRecord?.read(pcmBuffer, 0, pcmBuffer.size) ?: 0 } catch (_: Throwable) { 0 }
                if (read > 0) {
                    val pcmChunk = pcmBuffer.copyOf(read)
                    val packet = buildAudioPacket(chunkId, pcmChunk)
                    packetStore.writePacket(currentMeetingId, chunkId, packet)
                    val ok = webSocket?.send(ByteString.of(*packet)) ?: false
                    if (!ok) {
                        Log.w(TAG, "Failed to send live packet chunkId=$chunkId")
                    }
                    chunkId++
                }
            }
        }
    }

    private fun stopRecording() {
        isStreaming = false
        try {
            audioRecord?.let { r ->
                if (r.recordingState == AudioRecord.RECORDSTATE_RECORDING) r.stop()
                r.release()
            }
        } catch (_: Throwable) {}
        audioRecord = null
        recordJob?.cancel()
        recordJob = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun buildAudioPacket(chunkId: Long, audioBytes: ByteArray): ByteArray {
        // 메타 정보(JSON) 길이(4바이트) + JSON 바이트 + PCM 바이트 순으로 패킷을 구성합니다.
        val meta = JSONObject().apply {
            put("type", "audio")
            put("chunkId", chunkId)
            put("encoding", "pcm16")
            put("timestamp", LocalDateTime.now().toString())
        }
        val metaBytes = meta.toString().toByteArray(Charsets.UTF_8)
        val metaLen = metaBytes.size
        val buf = ByteBuffer.allocate(4 + metaLen + audioBytes.size)
        buf.putInt(metaLen)
        buf.put(metaBytes)
        buf.put(audioBytes)
        return buf.array()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleSocketMessage(envelope: SocketEnvelope, meetingId: Long) {
        when (envelope.type) {
            SocketType.CONNECTION_ESTABLISHED -> handleConnectionEstablished(envelope)
            SocketType.ERROR -> handleError(envelope)
            SocketType.MEETING_COMPLETED -> emit(RealtimeEvent.MeetingCompleted(meetingId = meetingId))
            SocketType.DIARIZED_SEGMENT -> emit(RealtimeEvent.DiarizedSegment(payloadJson = envelope.rawDataJson()))
            SocketType.MEETING_NOTE_CREATED -> emit(RealtimeEvent.MeetingNoteCreated)
            SocketType.MEETING_RECORD_MADED -> emit(RealtimeEvent.MeetingRecordMaded)
            SocketType.AGENDA_UPDATED -> emit(RealtimeEvent.AgendaUpdated(payloadJson = envelope.rawDataJson()))
            SocketType.UNKNOWN -> Log.d(TAG, "WebSocket unknown type: ${envelope.typeRaw}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleConnectionEstablished(envelope: SocketEnvelope) {
        val data = envelope.dataAs<ConnectionEstablishedData>()
        val lastServerChunkId = data?.lastProcessedChunkId ?: -1
        emit(
            RealtimeEvent.ConnectionEstablished(
                lastProcessedChunkId = lastServerChunkId,
                meetingStartTime = data?.meetingStartTime
            )
        )
        // 서버가 인지하지 못한 로컬 패킷을 먼저 보정 전송합니다.
        resendMissingLocalPackets(currentMeetingId, lastServerChunkId.toLong())
        // 로컬/서버 기준의 최대 chunkId 다음 값부터 송신을 재개합니다.
        val localMax = packetStore.findLastLocalChunkId(currentMeetingId)
        chunkId = maxOf(localMax, lastServerChunkId.toLong()) + 1
        // 실시간 녹음을 시작합니다.
        startRecording()
    }

    private fun handleError(envelope: SocketEnvelope) {
        val err = envelope.dataAs<ErrorData>()
        emit(RealtimeEvent.SocketError(message = err?.message.orEmpty()))
    }

    companion object {
        private const val TAG = "WebSocketClient"
    }
}


