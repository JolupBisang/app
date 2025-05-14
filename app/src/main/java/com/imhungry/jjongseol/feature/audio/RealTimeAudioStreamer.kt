package com.imhungry.jjongseol.feature.audio

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
import com.imhungry.jjongseol.data.network.client.WebSocketManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class RealTimeAudioStreamer(
    private val context: Context,
    private val userId: Long,
    private val meetingId: Long,
    private val webSocketManager: WebSocketManager?,
    private val cacheDir: File
) {
    private val sampleRate = 48000
    private val frameSize = 960
    private val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    ).coerceAtLeast(frameSize * 2)

    private var audioRecord: AudioRecord? = null
    private val encoder = OpusEncoderWrapper()
    private var isStreaming = false
    private var chunkId = 0
    private var isEncodingPaused = false

    fun pauseEncoding() { isEncodingPaused = true }
    fun resumeEncoding() { isEncodingPaused = false }

    @RequiresApi(Build.VERSION_CODES.O)
    fun start(scope: CoroutineScope) {
        Log.d("Audio", "start() 호출됨")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permission = Manifest.permission.RECORD_AUDIO
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                Log.e("Audio", "RECORD_AUDIO 권한 없음")
                return
            }
        }

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            ).apply {
                startRecording()
            }
            Log.d("Audio", "AudioRecord 생성 및 녹음 시작")
        } catch (e: Exception) {
            Log.e("Audio", "AudioRecord 생성 실패", e)
            return
        }

        encoder.init()
        isStreaming = true
        Log.d("Audio", "Opus 인코더 초기화 완료")

        scope.launch {
            val pcmBuffer = ByteArray(frameSize * 2)
            while (isActive && isStreaming) {
                val read = audioRecord?.read(pcmBuffer, 0, pcmBuffer.size) ?: 0
                if (read > 0 && !isEncodingPaused) {
                    val pcmChunk = pcmBuffer.copyOf(read)
                    encoder.encode(pcmChunk)?.let { opusData ->
                        val packet = buildPacket(opusData, userId, meetingId, chunkId++)
                        webSocketManager?.sendBinary(packet)
                        Log.d("Audio", "패킷 전송: chunkId=$chunkId, size=${packet.size}")
                        savePacketToFile(packet, chunkId)
                    }
                }
            }
        }
    }

    fun stop() {
        isStreaming = false
        audioRecord?.run {
            stop()
            release()
        }
        encoder.release()
        webSocketManager?.close()

        CoroutineScope(Dispatchers.IO).launch { deleteAllPackets() }
    }

    private fun savePacketToFile(packet: ByteArray, chunkId: Int) {
        runCatching {
            val dir = File(cacheDir, "audio_packets/$meetingId/$userId").apply { mkdirs() }
            File(dir, "packet_$chunkId.bin").outputStream().use { it.write(packet) }
        }.onFailure {
            Log.e("Audio", "패킷 저장 실패", it)
        }
    }

    private fun deleteAllPackets() {
        runCatching {
            val dir = File(cacheDir, "audio_packets/$meetingId/$userId")
            if (dir.exists()) {
                dir.listFiles()?.forEach { it.delete() }
                dir.delete()
                Log.d("Audio", "녹음 데이터 삭제 완료")
            }
        }.onFailure {
            Log.e("Audio", "삭제 실패", it)
        }
    }
}