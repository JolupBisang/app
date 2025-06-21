package com.imhungry.jjongseol.data.network.client

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.imhungry.jjongseol.data.model.agenda.dto.AgendaDto
import com.imhungry.jjongseol.data.model.segment.DiarizedSegment
import com.imhungry.jjongseol.data.model.response.ErrorResponse
import com.imhungry.jjongseol.data.model.response.SocketResponse
import com.imhungry.jjongseol.data.model.response.SocketResponseType
import com.imhungry.jjongseol.data.network.config.AppPrefs
import com.imhungry.jjongseol.data.repository.event.MeetingNoteEvent
import com.imhungry.jjongseol.data.repository.event.MeetingNoteEventBus
import com.imhungry.jjongseol.util.DateTimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.*
import okio.ByteString
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

class AudioWebSocketClient(
    private val context: Context,
    private val url: String,
    private val meetingId: Long,
    private val scope: CoroutineScope,
    private val onError: (String) -> Unit = {},
    private val onMessage: (String) -> Unit = {},
    private val onNewDiarizedSegment: (DiarizedSegment) -> Unit,
    private val isServiceStopped: () -> Boolean,
    private val onMeetingStartTime: ((Long) -> Unit)? = null,
    private val onAgendaUpdated: ((AgendaDto) -> Unit)? = null,
    var micEnabled: Boolean
) : WebSocketListener() {
    private var webSocket: WebSocket? = null
    private var chunkId: Long = 0
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var isStreaming = false
    private var isEncodingPaused = false
    private var isConnected = false

    private val sampleRate = 16000
    private val frameSize = 16000
    private val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    ).coerceAtLeast(frameSize)

    private var isReconnecting = false
    var isClosedByUser: Boolean = false
    private var isAudioClosedByMeetingCompleted: Boolean = false
    private var recordJob: Job? = null
    val appPrefs = AppPrefs(context)

    fun pauseEncoding() {
        stopRecording() // 녹음 자체를 멈춤
    }

    fun resumeEncoding() {
        startRecording(scope) // 녹음 자체를 시작
    }

    // /data/data/com.imhungry.jjongseol/cache/audio_packets/
    private val packetDir by lazy { File(context.cacheDir, "audio_packets/$meetingId") }
    // /storage/emulated/0/Android/data/com.imhungry.jjongseol/files/pcm_chunks/
    //private val chunkDir by lazy { File(context.getExternalFilesDir(null), "pcm_chunks/$meetingId") }
    // /storage/emulated/0/Android/data/com.imhungry.jjongseol/files/pcm_raw/
    //private val rawDir by lazy { File(context.getExternalFilesDir(null), "pcm_raw/$meetingId") }

    fun connect() {
        if (isAudioClosedByMeetingCompleted) {
            Log.d("Audio", "MEETING_COMPLETED 이후 재접속 시도 차단")
            return
        }
        if (isServiceStopped() || isClosedByUser) {
            Log.d("Audio", "서비스 중단됨: WebSocket 연결 시도 안 함")
            disconnect()
            return
        }
        isClosedByUser = false
        if (isConnected) disconnect()
        Log.d("Audio", "WebSocket 새로 연결")
        tryConnect()
    }

    private fun tryConnect() {
        if (isAudioClosedByMeetingCompleted) {
            Log.d("Audio", "MEETING_COMPLETED 이후 재접속 시도 차단")
            return
        }
        if (isServiceStopped() || isClosedByUser) {
            Log.d("Audio", "서비스 중단됨: WebSocket 연결 시도 안 함")
            disconnect()
            return
        }
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient.Builder().readTimeout(0, TimeUnit.MILLISECONDS).build()
        webSocket = client.newWebSocket(request, this)
        //startDummyDiarizedSegmentTest()
    }

    override fun onOpen(ws: WebSocket, response: Response) {
        isConnected = true
        isReconnecting = false
        Log.d("Audio", "WebSocket 연결됨")
    }

    override fun onMessage(ws: WebSocket, text: String) {
        Log.d("Audio", "onMessage 수신됨: $text")
        if (isServiceStopped() || isClosedByUser) {
            Log.d("Audio", "서비스 중단됨: WebSocket 연결 시도 안 함")
            disconnect()
            return
        }
        try {
            val response = Gson().fromJson(text, SocketResponse::class.java)
            when (response.type) {
                SocketResponseType.CONNECTION_ESTABLISHED -> {
                    val json = JSONObject(text)
                    val data = json.optJSONObject("data")
                    val lastChunkId = data?.optInt("lastProcessedChunkId", -1) ?: -1
                    val meetingStartTime = data?.optString("meetingStartTime")
                    Log.i("Audio", "CONNECTION_ESTABLISHED: chunkId=$lastChunkId, startTime=$meetingStartTime")
                    if (meetingStartTime != null) {
                        val db = FirebaseFirestore.getInstance()
                        val meetingRef = db.collection("meetings").document(meetingId.toString())
                        meetingRef.update("startTime", meetingStartTime)
                        val startTimeMillis = DateTimeUtils.isoToMillis(meetingStartTime)
                        appPrefs.setMeetingStartTime(meetingId, startTimeMillis)
                        onMeetingStartTime?.invoke(startTimeMillis)
                    }
                    preloadLocalPacketsAndThenStart(lastChunkId, scope)
                }
                SocketResponseType.ERROR -> {
                    val error = Gson().fromJson(Gson().toJson(response.data), ErrorResponse::class.java)
                    val message = error.message
                    Log.w("Socket", "WebSocket 에러 메시지 수신: $message")
                    if (!message.equals("진행중인 회의가 아닙니다.")) {
                    }
                }
                SocketResponseType.MEETING_COMPLETED -> {
                    val db = FirebaseFirestore.getInstance()
                    val meetingRef = db.collection("meetings").document(meetingId.toString())
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
                    val now = LocalDateTime.now()
                    val formatted = now.format(formatter)
                    meetingRef.update("endTime", formatted)
                    Log.i("Socket", "MEETING_COMPLETED 메시지 수신, 오디오 연결 종료")
                    isAudioClosedByMeetingCompleted = true
                    try {
                        audioRecord?.let { record ->
                            if (record.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                                record.stop()
                            }
                            record.release()
                        }
                    } catch (e: Exception) { }
                    audioRecord = null

                    recordJob?.cancel()
                    recordJob = null
                    stopRecording()
                    onMessage("MEETING_COMPLETED")
                    scope.launch(Dispatchers.IO) {
                        delay(500)
                        deleteAllPackets()
                    }
                    MeetingNoteEventBus.send(MeetingNoteEvent.Created(meetingId))
                }
                SocketResponseType.DIARIZED_SEGMENT -> {
                    val message = Gson().fromJson(Gson().toJson(response.data), DiarizedSegment::class.java)
                    onNewDiarizedSegment(message)
                }
                SocketResponseType.MEETING_NOTE_CREATED -> {
                    Log.i("Socket", "MEETING_NOTE_CREATED 회의록 완성")
                    try {
                        audioRecord?.let { record ->
                            if (record.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                                record.stop()
                            }
                            record.release()
                        }
                    } catch (e: Exception) { }
                    audioRecord = null

                    recordJob?.cancel()
                    recordJob = null
                    stop()
                    onMessage("MEETING_RECORD_MADED")
                    MeetingNoteEventBus.send(MeetingNoteEvent.Completed(meetingId))
                }
                SocketResponseType.MEETING_RECORD_MADED -> {
                    Log.i("Socket", "MEETING_RECORD_MADED 메시지 수신, 모든 연결 종료")
                }
                SocketResponseType.AGENDA_UPDATED -> {
                    val json = JSONObject(text)
                    val type = json.optString("type")
                    val data = json.optJSONObject("data")
                    val updated = Gson().fromJson(data.toString(), AgendaDto::class.java)
                    onAgendaUpdated?.invoke(updated)
                }
                else -> {
                    Log.d("Socket", "Socket 응답 : ${response.type}")
                }
            }
        } catch (e: Exception) {
            Log.w("Socket", e)
        }
    }

    override fun onClosed(ws: WebSocket, code: Int, reason: String) {
        Log.i("Socket", "WebSocket 닫힘 $code/$reason")
        disconnect()
        if (isServiceStopped()) {
            Log.d("Socket", "서비스 중단됨: WebSocket 연결 시도 안 함")
            disconnect()
            return
        }
        tryReconnect()
    }

    override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
        Log.e("Socket", "WebSocket 실패: ${t.message}")
        stopRecording()
        //onError("서버 내부 오류입니다. 관리자에게 문의해주세요.")
        tryReconnect()
    }

    private fun tryReconnect() {
        if (isServiceStopped() || isClosedByUser || isReconnecting || isConnected)  {
            disconnect()
            return
        }
        isReconnecting = true
        scope.launch {
            delay(2000)
            if (!isConnected && !isClosedByUser) {
                Log.w("Socket", "WebSocket 재연결 시도...")
                tryConnect()
            }
        }
    }

    private fun resendMissingLocalPackets(lastServerChunkId: Long, onComplete: () -> Unit) {
        packetDir.mkdirs()
        scope.launch(Dispatchers.IO) {
            for (id in (lastServerChunkId + 1)..(findLastLocalChunkId())) {
                val file = File(packetDir, "packet_$id.bin")
                if (!file.exists()) break
                val data = file.readBytes()
                sendBinary(data)
                Log.d("Audio", "로컬 패킷 재전송: packet_$id.bin")
            }
            onComplete()
        }
    }

    private fun findLastLocalChunkId(): Long {
        packetDir.mkdirs()
        return packetDir.listFiles()
            ?.mapNotNull { it.name.removePrefix("packet_").removeSuffix(".bin").toLongOrNull() }
            ?.maxOrNull() ?: -1L
    }

    fun preloadLocalPacketsAndThenStart(lastServerChunkId: Int, scope: CoroutineScope) {
        if (isAudioClosedByMeetingCompleted) {
            Log.d("Socket", "MEETING_COMPLETED 이후 재접속 시도 차단")
            return
        }
        setInitialChunkId(lastServerChunkId)
        resendMissingLocalPackets(lastServerChunkId.toLong()) {
            if (micEnabled) {
                startRecording(scope)
            } else {
                Log.d("Socket", "micEnabled=false 상태이므로 녹음 시작 안함")
            }
        }
    }

    private fun setInitialChunkId(lastServerChunkId: Int) {
        val localMax = findLastLocalChunkId()
        chunkId = (maxOf(lastServerChunkId.toLong(), localMax) + 1)
        Log.d("Socket", "초기 chunkId 설정됨: $chunkId (server=$lastServerChunkId, local=$localMax)")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startRecording(scope: CoroutineScope) {
        if (!hasRecordPermission()) {
            Log.e("Socket", "RECORD_AUDIO 권한 없음")
            return
        }
        if (isAudioClosedByMeetingCompleted) {
            Log.d("Socket", "MEETING_COMPLETED 이후 재접속 시도 차단")
            return
        }
        if (audioRecord != null && audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
            Log.w("Socket", "이미 녹음이 진행 중입니다. 중복 생성 방지")
            return
        }
        if (isAudioClosedByMeetingCompleted) {
            Log.d("Socket", "MEETING_COMPLETED 이후 재접속 시도 차단")
            return
        }
        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            ).apply { startRecording() }

            Log.d("Socket", "AudioRecord 생성 및 녹음 시작")

        } catch (e: SecurityException) {
            Log.e("Socket", "RECORD_AUDIO 권한이 없어 AudioRecord 생성 실패", e)
            return
        } catch (e: Exception) {
            Log.e("Socket", "AudioRecord 생성 실패", e)
            return
        }

        isStreaming = true

        val rawDir = File(context.getExternalFilesDir(null), "pcm_raw/$meetingId")
        rawDir.mkdirs()
        val rawPcmFile = File(rawDir, "all_raw.pcm")
        val rawPcmOutput = FileOutputStream(rawPcmFile, true)

        recordJob?.cancel()
        recordJob = scope.launch {
            val pcmBuffer = ByteArray(frameSize * 2)
            while (isActive && isStreaming) {
                val read = audioRecord?.read(pcmBuffer, 0, pcmBuffer.size) ?: 0
                if (read > 0 && !isEncodingPaused) {
                    if (audioRecord != null && audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                        Log.w("Audio", "녹음 진행 중")
                    }
                    val pcmChunk = pcmBuffer.copyOf(read)
                    // 1. 전체 raw 저장
                    try {
                        rawPcmOutput.write(pcmChunk)
                    } catch (e: Exception) {
                    }
                    // 2. 청크별 저장
//                    try {
//                        val chunkFile = File(chunkDir, "chunk_${chunkId}.pcm")
//                        chunkFile.parentFile?.mkdirs()
//                        chunkFile.outputStream().use { it.write(pcmChunk) }
//                    } catch (e: Exception) {
//                    }
                    // 3. 서버에 보낼 패킷 생성
                    val packet = buildAudioPacket(chunkId, pcmChunk)
                    // 4. 패킷 파일로 저장
                    try {
                        val packetFile = File(packetDir, "packet_${chunkId}.bin")
                        packetFile.parentFile?.mkdirs()
                        packetFile.outputStream().use { it.write(packet) }
                    } catch (e: Exception) {
                        Log.w("Audio", "packetFile 저장 실패: ${e.message}", e)
                    }
                    // 5. 서버로 송신
                    sendBinary(packet)
                    Log.d("Audio", "오디오 데이터 전송: chunkId=$chunkId, size=${packet.size}")
                    chunkId++
                }
            }
            try {
                rawPcmOutput.close()
            } catch (e: Exception) {
            }
        }
    }

    private fun buildAudioPacket(chunkId: Long, audioBytes: ByteArray): ByteArray {
        val now = LocalDateTime.now()
        val meta = JSONObject().apply {
            put("type", "audio")
            put("chunkId", chunkId)
            put("encoding", "opus")
            put("timestamp", DateTimeUtils.koreaToUtcTime(now.toString()))
        }
        val metaBytes = meta.toString().toByteArray(Charsets.UTF_8)
        val metaLen = metaBytes.size
        val buf = ByteBuffer.allocate(4 + metaLen + audioBytes.size)
        buf.putInt(metaLen)
        buf.put(metaBytes)
        buf.put(audioBytes)
        return buf.array()
    }

    fun sendBinary(data: ByteArray) {
        val ok = webSocket?.send(ByteString.of(*data)) ?: false
        if (!ok) Log.e("Audio", "WebSocket 바이너리 전송 실패")
    }

//    fun stop() {
//        isClosedByUser = true
//        isStreaming = false
//        try {
//            audioRecord?.let { record ->
//                if (record.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
//                    record.stop()
//                }
//                record.release()
//            }
//        } catch (e: IllegalStateException) {
//            Log.e("Audio", "AudioRecord stop() 실패: ${e.message}", e)
//        }
//        audioRecord = null
//
//        webSocket?.close(1000, "Normal closure")
//        webSocket = null
//        isConnected = false
//    }

    fun stop() {
        isClosedByUser = true
        isStreaming = false
        isAudioClosedByMeetingCompleted = true
        try {
            audioRecord?.let { record ->
                if (record.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    record.stop()
                }
                record.release()
            }
        } catch (e: IllegalStateException) {
            Log.e("Audio", "AudioRecord stop() 실패: ${e.message}", e)
        }
        audioRecord = null

        recordJob?.cancel()
        recordJob = null

        webSocket?.close(1000, "Normal closure")
        webSocket = null
        isConnected = false
    }


    fun deleteAllPackets() {
        scope.launch(Dispatchers.IO) {
            runCatching {
                packetDir.listFiles()?.forEach { it.delete() }
                packetDir.delete()
//                chunkDir.listFiles()?.forEach { it.delete() }
//                chunkDir.delete()
//                rawDir.listFiles()?.forEach { it.delete() }
//                rawDir.delete()
            }
        }
        Log.d("Audio","모든 패킷 삭제 완료")
    }

    private fun stopRecording() {
        isRecording = false
        try {
            audioRecord?.let { record ->
                if (record.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    record.stop()
                }
                record.release()
            }
        } catch (e: Exception) { }
        audioRecord = null

        recordJob?.cancel()
        recordJob = null
    }

    fun disconnect() {
        isClosedByUser = true
        stopRecording()
        webSocket?.close(1000, "Normal closure")
        webSocket = null
        isConnected = false
    }

    private fun hasRecordPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        } else true
    }


    fun startDummyDiarizedSegmentTest() {
        scope.launch {
            var order = 1
            while (isActive) {
                val dummySegment = DiarizedSegment(
                    userId = 1L,
                    text = "이것은 테스트 대사 $order 입니다.",
                    timestamp = "2025-06-12T15:03:12.000000",
                    order = order,
                )
                onNewDiarizedSegment(dummySegment)
                order++
                delay(2000) // 2초
            }
        }
    }

}

