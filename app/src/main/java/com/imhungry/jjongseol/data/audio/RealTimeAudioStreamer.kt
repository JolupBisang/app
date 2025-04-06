package com.imhungry.jjongseol.data.audio

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.imhungry.jjongseol.data.network.WebSocketManager
import com.imhungry.jjongseol.util.OpusEncoderWrapper
import com.imhungry.jjongseol.util.buildPacket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class RealTimeAudioStreamer(
    private val userId: Long,
    private val meetingId: Long,
    private val webSocketManager: WebSocketManager,
    private val cacheDir: File
) {
    private val sampleRate = 16000
    private val frameSize = 320
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
        Log.d("Audio", "스트리밍 시작 준비 중...")

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )
        } catch (e: SecurityException) {
            Log.e("Audio", "AudioRecord 생성 실패: 권한 부족", e)
            return
        } catch (e: IllegalArgumentException) {
            Log.e("Audio", "AudioRecord 파라미터 오류", e)
            return
        }

        encoder.init()
        audioRecord?.startRecording()
        isStreaming = true
        Log.d("Audio", "녹음 시작됨")

        scope.launch(Dispatchers.IO) {
            val pcmBuffer = ByteArray(frameSize * 2)
            while (isActive && isStreaming) {
                val read = audioRecord?.read(pcmBuffer, 0, pcmBuffer.size) ?: 0
                if (read > 0 && !isEncodingPaused) {
                    val pcmChunk = pcmBuffer.copyOf(read)
                    val opusData = encoder.encode(pcmChunk)
                    opusData?.let {
                        val packet = buildPacket(it, userId, meetingId, chunkId++)
                        webSocketManager.sendBinary(packet)
                        savePacketToFile(packet, chunkId)
                    }
                }
            }
        }
    }

    private fun savePacketToFile(packet: ByteArray, chunkId: Int) {
        try {
            val dir = File(cacheDir, "audio_packets/$meetingId/$userId")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, "packet_$chunkId.bin")
            FileOutputStream(file).use { it.write(packet) }
            Log.d("Audio", "패킷 저장: ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e("Audio", "패킷 저장 실패", e)
        }
    }

    fun stop() {
        isStreaming = false
        audioRecord?.stop()
        audioRecord?.release()
        encoder.release()
        webSocketManager.close()

        CoroutineScope(Dispatchers.IO).launch {
            deleteAllPackets()
        }

        Log.d("Audio", "스트리밍 종료")
    }

    private fun deleteAllPackets() {
        try {
            val dir = File(cacheDir, "audio_packets/$meetingId/$userId")
            if (dir.exists()) {
                dir.listFiles()?.forEach { it.delete() }
                dir.delete()
                Log.d("Audio", "모든 패킷 삭제 완료: ${dir.absolutePath}")
            }
        } catch (e: Exception) {
            Log.e("Audio", "패킷 삭제 실패", e)
        }
    }
}