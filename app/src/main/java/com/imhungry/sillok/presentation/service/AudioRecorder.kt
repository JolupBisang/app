package com.imhungry.sillok.presentation.service

import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.imhungry.sillok.presentation.util.DateTimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.WebSocket
import okio.ByteString.Companion.toByteString
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.coroutineContext

/**
 * 오디오 녹음 및 전송을 담당하는 클래스
 */
@RequiresApi(Build.VERSION_CODES.O)
class AudioRecorder(
    private val context: Context,
    private val serviceScope: CoroutineScope,
    private val packetDir: File?,
    private val chunkIdCounter: AtomicLong
) {
    companion object {
        private const val TAG = "AudioRecorder"
    }

    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private var isRecording = false
    var micEnabled = true
        private set

    private val sampleRate = 16000
    private val frameSize = 16000
    private val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    ).coerceAtLeast(frameSize)

    fun startRecording(webSocket: WebSocket) {
        Log.d(TAG, "[Recording-1] 녹음 시작 요청")
        if (isRecording) {
            Log.d(TAG, "[Recording-1 스킵] 이미 녹음 중입니다")
            return
        }

        // 권한 체크
        Log.d(TAG, "[Recording-1-1] 오디오 녹음 권한 확인")
        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "[Recording-1-1 실패] 오디오 녹음 권한이 없습니다")
            return
        }
        Log.d(TAG, "[Recording-1-1 완료] 오디오 녹음 권한 확인 완료")

        try {
            Log.d(
                TAG,
                "[Recording-1-2] AudioRecord 초기화 시작: sampleRate=$sampleRate, bufferSize=$bufferSize"
            )
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "[Recording-1-2 실패] AudioRecord 초기화 실패")
                audioRecord?.release()
                audioRecord = null
                return
            }
            Log.d(TAG, "[Recording-1-2 완료] AudioRecord 초기화 성공")

            Log.d(TAG, "[Recording-1-3] AudioRecord 녹음 시작")
            audioRecord?.startRecording()
            isRecording = true
            Log.d(TAG, "[Recording-1-3 완료] AudioRecord 녹음 시작 완료")

            Log.d(TAG, "[Recording-1-4] 오디오 데이터 읽기 Job 시작")
            recordingJob = serviceScope.launch(Dispatchers.IO) {
                readAndSendAudioData(webSocket)
            }
            Log.d(TAG, "[Recording-1 완료] 녹음 시작 완료")
        } catch (e: Exception) {
            Log.e(TAG, "[Recording-1 실패] 녹음 시작 실패: ${e.message}", e)
        }
    }

    private suspend fun readAndSendAudioData(webSocket: WebSocket) {
        Log.d(TAG, "[Audio-1] 오디오 데이터 읽기 및 전송 시작")
        val chunkSizeInBytes = frameSize * 2
        val buffer = ByteArray(chunkSizeInBytes)
        var chunkCount = 0L
        var lastLogTime = System.currentTimeMillis()
        val logInterval = 5000L // 5초마다 로그

        try {
            while (isRecording &&
                audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING &&
                coroutineContext.isActive
            ) {
                // 항상 오디오 데이터 읽기 (버퍼 오버플로우 방지)
                val bytesRead = audioRecord?.read(buffer, 0, buffer.size) ?: 0

                if (bytesRead > 0) {
                    // 마이크가 켜져있을 때만 패킷 생성 및 전송
                    if (micEnabled) {
                        val audioChunk = buffer.copyOf(bytesRead)
                        sendAudioChunk(webSocket, audioChunk)
                        chunkCount++

                        // 주기적으로 로그 출력 (5초마다)
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastLogTime >= logInterval) {
                            Log.d(TAG, "[Audio-2] 음성 패킷 전송 중: 총 ${chunkCount}개 전송됨 (마지막 5초간)")
                            lastLogTime = currentTime
                        }
                    }
                    // micEnabled가 false일 때는 읽은 데이터를 버리고 패킷 생성/전송하지 않음
                } else if (bytesRead < 0) {
                    Log.w(TAG, "[Audio-경고] 오디오 읽기 실패: bytesRead=$bytesRead")
                }
            }
            Log.d(TAG, "[Audio-1 완료] 오디오 데이터 읽기 및 전송 종료: 총 ${chunkCount}개 전송됨")
        } catch (e: CancellationException) {
            Log.d(TAG, "[Audio-취소] 오디오 읽기 취소됨: 총 ${chunkCount}개 전송됨")
        } catch (e: Exception) {
            Log.e(TAG, "[Audio-에러] 오디오 읽기 에러: ${e.message} (총 ${chunkCount}개 전송됨)", e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendAudioChunk(webSocket: WebSocket, audioData: ByteArray) {
        try {
            val chunkId = chunkIdCounter.getAndIncrement()

            val metaJson = JSONObject().apply {
                put("type", "AUDIO_CHUNK")
                put("chunkId", chunkId)
                put("encoding", "audio/pcm")
                put("timestamp", DateTimeUtils.getCurrentUtcTime())
            }

            val metaBytes = metaJson.toString().toByteArray(Charsets.UTF_8)
            val metaLength = metaBytes.size

            val buffer = ByteBuffer.allocate(4 + metaLength + audioData.size)
            buffer.putInt(metaLength)
            buffer.put(metaBytes)
            buffer.put(audioData)

            val totalSize = buffer.position()
            val binaryMessage = buffer.array().toByteString(0, totalSize)

            // WebSocket으로 전송
            val sendSuccess = webSocket.send(binaryMessage)
            if (!sendSuccess) {
                Log.w(
                    TAG,
                    "[Audio-Chunk-경고] 청크 전송 실패: chunkId=$chunkId, size=${audioData.size} bytes (큐가 가득 참)"
                )
            }

            // 로컬 파일로 저장
            packetDir?.let { dir ->
                try {
                    val audioFile = File(dir, "chunk_${chunkId}.pcm")
                    FileOutputStream(audioFile).use { it.write(audioData) }
                } catch (e: Exception) {
                    Log.e(TAG, "[Audio-Chunk-저장실패] 청크 파일 저장 실패: chunkId=$chunkId, ${e.message}", e)
                }
            }

            // 첫 번째 청크와 주기적으로 로그 출력 (100개마다)
            if (chunkId == 0L || chunkId % 100 == 0L) {
                Log.d(
                    TAG,
                    "[Audio-Chunk] 청크 전송: chunkId=$chunkId, size=${audioData.size} bytes, totalSize=$totalSize bytes, success=$sendSuccess"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "[Audio-Chunk-예외] 청크 전송 예외: ${e.message}", e)
        }
    }

    fun stopRecording() {
        if (!isRecording) return

        isRecording = false
        recordingJob?.cancel()
        recordingJob = null

        try {
            audioRecord?.apply {
                if (recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    stop()
                }
                release()
            }
            audioRecord = null
            Log.d(TAG, "녹음 중지 완료")
        } catch (e: Exception) {
            Log.e(TAG, "녹음 중지 중 에러 발생", e)
        }
    }

    fun toggleMic() {
        micEnabled = !micEnabled
        Log.d(TAG, "마이크 토글: ${if (micEnabled) "켜짐" else "꺼짐"}")
    }
}

