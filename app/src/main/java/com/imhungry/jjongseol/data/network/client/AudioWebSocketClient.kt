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
import com.google.gson.Gson
import com.imhungry.jjongseol.data.model.response.ErrorResponse
import com.imhungry.jjongseol.data.model.response.SocketResponse
import com.imhungry.jjongseol.data.model.response.SocketResponseType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.*
import okio.ByteString
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

class AudioWebSocketClient(
    private val context: Context,
    private val url: String,
    private val meetingId: Long,
    private val scope: CoroutineScope,
    private val onError: (String) -> Unit = {},
    private val onMessage: (String) -> Unit = {},
) : WebSocketListener() {

    private var webSocket: WebSocket? = null
    private var chunkId: Long = 0
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var isStreaming = false
    private var isEncodingPaused = false
    private var isConnected = false

    private val sampleRate = 16000
    private val frameSize = 10912
    private val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    ).coerceAtLeast(frameSize * 2)

    //fun pauseEncoding() { isEncodingPaused = true }
    //fun resumeEncoding() { isEncodingPaused = false }

    private val packetDir by lazy { File(context.cacheDir, "audio_packets/$meetingId") }
    private val chunkDir by lazy { File(context.getExternalFilesDir(null), "pcm_chunks/$meetingId") }
    private val rawDir by lazy { File(context.getExternalFilesDir(null), "pcm_raw/$meetingId") }

    fun connect() {
        if (isConnected) disconnect()

        val request = Request.Builder()
            .url(url)
            .build()

        val client = OkHttpClient.Builder().readTimeout(0, TimeUnit.MILLISECONDS).build()
        webSocket = client.newWebSocket(request, this)
    }

    override fun onOpen(ws: WebSocket, response: Response) {
        isConnected = true
        Log.d("Audio", "WebSocket 연결됨")
    }

    override fun onMessage(ws: WebSocket, text: String) {
        Log.d("Audio", "onMessage 수신됨: $text")

        try {
            val response = Gson().fromJson(text, SocketResponse::class.java)
            when (response.type) {
                SocketResponseType.LAST_PROCESSED_CHUNK_ID -> {
                    val lastChunkId = (response.data as Double).toInt()
                    Log.i("Audio", "마지막 chunkId 수신됨: $lastChunkId")
                    preloadLocalPacketsAndThenStart(lastChunkId, scope)
                }
                SocketResponseType.ERROR -> {
                    val error = Gson().fromJson(Gson().toJson(response.data), ErrorResponse::class.java)
                    val message = error.message ?: "알 수 없는 오류가 발생했습니다"
                    Log.w("Audio", "WebSocket 에러 메시지 수신: $message")
                    onError(message)
                }
                else -> Log.d("Audio", "알 수 없는 메시지 타입 수신: ${response.type}")
            }
        } catch (e: Exception) {
            Log.w("Audio", "SocketResponse 파싱 실패. 일반 텍스트로 처리", e)
            onMessage(text)
        }
    }

    override fun onClosed(ws: WebSocket, code: Int, reason: String) {
        isConnected = false
        Log.i("Audio", "WebSocket 닫힘 $code/$reason")
        stopRecording()
    }

    override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
        isConnected = false
        Log.e("Audio", "WebSocket 실패: ${t.message}")
        stopRecording()
        onError(t.message ?: "WebSocket 오류")
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
        setInitialChunkId(lastServerChunkId)
        resendMissingLocalPackets(lastServerChunkId.toLong()) {
            startRecording(scope)
        }
    }

    private fun setInitialChunkId(lastServerChunkId: Int) {
        val localMax = findLastLocalChunkId()
        chunkId = (maxOf(lastServerChunkId.toLong(), localMax) + 1)
        Log.d("Audio", "초기 chunkId 설정됨: $chunkId (server=$lastServerChunkId, local=$localMax)")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startRecording(scope: CoroutineScope) {
        if (!hasRecordPermission()) {
            Log.e("Audio", "RECORD_AUDIO 권한 없음")
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

            Log.d("Audio", "AudioRecord 생성 및 녹음 시작")

        } catch (e: SecurityException) {
            Log.e("Audio", "RECORD_AUDIO 권한이 없어 AudioRecord 생성 실패", e)
            return
        } catch (e: Exception) {
            Log.e("Audio", "AudioRecord 생성 실패", e)
            return
        }

        isStreaming = true

        val rawDir = File(context.getExternalFilesDir(null), "pcm_raw/$meetingId")
        rawDir.mkdirs()
        val rawPcmFile = File(rawDir, "all_raw.pcm")
        val rawPcmOutput = FileOutputStream(rawPcmFile, true)

        scope.launch {
            val pcmBuffer = ByteArray(frameSize * 2)
            while (isActive && isStreaming) {
                val read = audioRecord?.read(pcmBuffer, 0, pcmBuffer.size) ?: 0
                if (read > 0 && !isEncodingPaused) {
                    val pcmChunk = pcmBuffer.copyOf(read)
                    // 1. 전체 raw 저장
                    rawPcmOutput.write(pcmChunk)
                    // 2. 청크별 저장
                    val chunkFile = File(chunkDir, "chunk_${chunkId}.pcm")
                    chunkFile.parentFile?.mkdirs()
                    chunkFile.outputStream().use { it.write(pcmChunk) }
                    // 3. 서버에 보낼 패킷 생성
                    val packet = buildAudioPacket(chunkId, pcmChunk)
                    // 4. 패킷 파일로 저장
                    File(packetDir, "packet_${chunkId}.bin").outputStream().use { it.write(packet) }
                    // 5. 서버로 송신
                    sendBinary(packet)
                    Log.d("Audio", "오디오 데이터 전송: chunkId=$chunkId, size=${packet.size}")
                    chunkId++
                }
            }
            rawPcmOutput.close()
        }
    }

    private fun buildAudioPacket(chunkId: Long, audioBytes: ByteArray): ByteArray {
        val now = LocalDateTime.now()
        val meta = JSONObject().apply {
            put("type", "pcm")
            put("chunkId", chunkId)
            put("encoding", "audio/pcm")
            put("timestamp", now.toString())
        }
        val metaBytes = meta.toString().toByteArray(Charsets.UTF_8)
        val metaLen = metaBytes.size
        val buf = ByteBuffer.allocate(4 + metaLen + audioBytes.size).order(ByteOrder.BIG_ENDIAN)
        buf.putInt(metaLen)
        buf.put(metaBytes)
        buf.put(audioBytes)
        return buf.array()
    }

    fun sendBinary(data: ByteArray) {
        val ok = webSocket?.send(ByteString.of(*data)) ?: false
        if (!ok) Log.e("Audio", "WebSocket 바이너리 전송 실패")
    }

    fun stop(deleteLocalPackets: Boolean = false) {
        isStreaming = false
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

        webSocket?.close(1000, "Normal closure")
        webSocket = null
        isConnected = false

        if (deleteLocalPackets) {
            scope.launch(Dispatchers.IO) { deleteAllPackets() }
        }
    }

    fun deleteAllPackets() {
        scope.launch(Dispatchers.IO) {
            runCatching {
                packetDir.listFiles()?.forEach { it.delete() }
                packetDir.delete()
                chunkDir.listFiles()?.forEach { it.delete() }
                chunkDir.delete()
                rawDir.listFiles()?.forEach { it.delete() }
                rawDir.delete()
            }
        }
    }

    fun close() {
        if (isConnected) {
            webSocket?.close(1000, "Normal closure")
            webSocket = null
            isConnected = false
        }
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
    }

    fun disconnect() {
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
}
