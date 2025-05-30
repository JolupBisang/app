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
import java.io.FileOutputStream

class RealTimeAudioStreamer(
    private val context: Context,
    private val userId: Long,
    private val meetingId: Long,
    private val webSocketManager: WebSocketManager?,
    private val cacheDir: File
) {
    companion object {
        private const val TAG = "AudioStreamer"
    }

    private val sampleRate = 48000
    private val frameSize = 32768
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

    fun preloadLocalPacketsAndThenStart(lastServerChunkId: Int, scope: CoroutineScope) {
        setInitialChunkId(lastServerChunkId)
        resendMissingLocalPackets(lastServerChunkId, scope) {
            startStreaming(scope)
        }
    }

    private fun setInitialChunkId(lastServerChunkId: Int) {
        val localMax = findLastLocalChunkId()
        chunkId = maxOf(lastServerChunkId, localMax) + 1
        Log.d(TAG, "초기 chunkId 설정됨: $chunkId (server=$lastServerChunkId, local=$localMax)")
    }

    private fun resendMissingLocalPackets(lastServerChunkId: Int, scope: CoroutineScope, onComplete: () -> Unit) {
        val dir = File(cacheDir, "audio_packets/$meetingId/$userId")
        val start = lastServerChunkId + 1

        scope.launch {
            for (id in start..start + 10000) {
                val file = File(dir, "packet_$id.bin")
                if (!file.exists()) break
                val data = file.readBytes()
                webSocketManager?.sendBinary(data)
                Log.d(TAG, "로컬 패킷 전송: packet_$id.bin")
            }
            onComplete()
        }
    }

    /*@RequiresApi(Build.VERSION_CODES.O)
    private fun startStreaming(scope: CoroutineScope) {
        if (!hasRecordPermission()) {
            Log.e(TAG, "RECORD_AUDIO 권한 없음")
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

            Log.d(TAG, "AudioRecord 생성 및 녹음 시작")

        } catch (e: SecurityException) {
            Log.e(TAG, "RECORD_AUDIO 권한이 없어 AudioRecord 생성 실패", e)
            return
        } catch (e: Exception) {
            Log.e(TAG, "AudioRecord 생성 실패", e)
            return
        }

        encoder.init()
        isStreaming = true
        Log.d(TAG, "Opus 인코더 초기화 완료")

        scope.launch {
            val pcmBuffer = ByteArray(frameSize * 2)
            while (isActive && isStreaming) {
                val read = audioRecord?.read(pcmBuffer, 0, pcmBuffer.size) ?: 0
                if (read > 0 && !isEncodingPaused) {
                    val pcmChunk = pcmBuffer.copyOf(read)

                    val encodeStart = System.currentTimeMillis()
                    val opusBytes = FFmpegOpusUtil.encodePcmToOpus(
                        pcmData = pcmChunk,
                        sampleRate = sampleRate,
                        channels = channels,
                        cacheDir = cacheDir,
                        chunkId = chunkId
                    )
                    val encodeEnd = System.currentTimeMillis()
                    val encodeTime = encodeEnd - encodeStart

                    if (opusBytes != null) {
                        val compressionRatio = opusBytes.size.toFloat() / pcmChunk.size.toFloat() * 100
                        Log.d("Audio", "압축 시간 : ${encodeTime}ms")
                        Log.d("Audio", "PCM size: ${pcmChunk.size} bytes → Opus size: ${opusBytes.size} bytes")

                        try {
                            val opusDir = File(context.getExternalFilesDir(null), "opus/$meetingId/$userId")
                            opusDir.mkdirs()
                            val opusFile = File(opusDir, "chunk_${chunkId}.opus")
                            FileOutputStream(opusFile).use { it.write(opusBytes) }
                            Log.d("Audio", "Opus 파일 저장 완료: ${opusFile.absolutePath}")
                        } catch (e: Exception) {
                            Log.e("Audio", "Opus 파일 저장 실패", e)
                        }

                        val packet = buildPacket(opusBytes, userId, meetingId, chunkId)
                        webSocketManager?.sendBinary(packet)
                        Log.d(TAG, "실시간 패킷 전송: chunkId=$chunkId, size=${packet.size}")
                        savePacketToFile(packet, chunkId)
                        chunkId++
                    }
                }
            }
        }
    }*/

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startStreaming(scope: CoroutineScope) {
        if (!hasRecordPermission()) {
            Log.e(TAG, "RECORD_AUDIO 권한 없음")
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

            Log.d(TAG, "AudioRecord 생성 및 녹음 시작")

        } catch (e: SecurityException) {
            Log.e(TAG, "RECORD_AUDIO 권한이 없어 AudioRecord 생성 실패", e)
            return
        } catch (e: Exception) {
            Log.e(TAG, "AudioRecord 생성 실패", e)
            return
        }

        encoder.init()
        isStreaming = true
        Log.d(TAG, "Opus 인코더 초기화 완료")

        val rawDir = File(context.getExternalFilesDir(null), "pcm_raw/$meetingId/$userId")
        rawDir.mkdirs()
        val rawPcmFile = File(rawDir, "all_raw.pcm")
        val rawPcmOutput = FileOutputStream(rawPcmFile, /*append=*/true)

        scope.launch {
            val pcmBuffer = ByteArray(frameSize * 2)
            while (isActive && isStreaming) {
                val read = audioRecord?.read(pcmBuffer, 0, pcmBuffer.size) ?: 0
                if (read > 0 && !isEncodingPaused) {
                    val pcmChunk = pcmBuffer.copyOf(read)

                    rawPcmOutput.write(pcmChunk)

                    try {
                        val chunkDir = File(context.getExternalFilesDir(null), "pcm_chunks/$meetingId/$userId")
                        chunkDir.mkdirs()
                        val chunkFile = File(chunkDir, "chunk_${chunkId}.pcm")
                        FileOutputStream(chunkFile).use { it.write(pcmChunk) }
                    } catch (e: Exception) {
                        Log.e(TAG, "청크 PCM 파일 저장 실패", e)
                    }

                    // (아래는 기존 opus 변환/패킷 전송 등 기존 코드)
                    // ...
                    chunkId++
                }
            }
            rawPcmOutput.close()
        }
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
            Log.e(TAG, "AudioRecord stop() 실패: ${e.message}", e)
        }

        encoder.release()
        webSocketManager?.close()

        if (deleteLocalPackets) {
            CoroutineScope(Dispatchers.IO).launch { deleteAllPackets() }
        }
    }

    private fun savePacketToFile(packet: ByteArray, chunkId: Int) {
        runCatching {
            val dir = File(cacheDir, "audio_packets/$meetingId/$userId").apply { mkdirs() }
            File(dir, "packet_$chunkId.bin").outputStream().use { it.write(packet) }
        }.onFailure {
            Log.e(TAG, "패킷 저장 실패", it)
        }
    }

    private fun deleteAllPackets() {
        runCatching {
            val dir = File(cacheDir, "audio_packets/$meetingId/$userId")
            if (dir.exists()) {
                dir.listFiles()?.forEach { it.delete() }
                dir.delete()
                Log.d(TAG, "로컬 녹음 데이터 삭제 완료")
            }
        }.onFailure {
            Log.e(TAG, "로컬 패킷 삭제 실패", it)
        }
    }

    private fun findLastLocalChunkId(): Int {
        val dir = File(cacheDir, "audio_packets/$meetingId/$userId")
        return dir.listFiles()
            ?.mapNotNull {
                it.name.removePrefix("packet_").removeSuffix(".bin").toIntOrNull()
            }
            ?.maxOrNull() ?: -1
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
