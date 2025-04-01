package com.imhungry.jjongseol.data.audio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class AudioRecorder(
    private val context: Context,
    private val outputFile: File
) {
    private var isRecording = false
    private val sampleRate = 16000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

    suspend fun startRecording() = withContext(Dispatchers.IO) {
        if (!hasRecordPermission()) {
            Log.e("AudioRecorder", "RECORD_AUDIO 권한이 없습니다.")
            return@withContext
        }

        val recorder = try {
            AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )
        } catch (e: SecurityException) {
            Log.e("AudioRecorder", "보안 예외 발생", e)
            return@withContext
        } catch (e: IllegalArgumentException) {
            Log.e("AudioRecorder", "AudioRecord 초기화 실패", e)
            return@withContext
        }

        val buffer = ByteArray(bufferSize)
        recorder.startRecording()
        isRecording = true
        Log.d("Audio", "녹음 시작")

        FileOutputStream(outputFile).use { fos ->
            while (isRecording) {
                val read = recorder.read(buffer, 0, buffer.size)
                if (read > 0) {
                    fos.write(buffer, 0, read)
                    Log.d("Audio", "오디오 ${read}바이트 기록됨")
                }
            }
        }

        recorder.stop()
        recorder.release()
        Log.d("Audio", "녹음 종료 및 리소스 해제")
    }

    fun stopRecording() {
        isRecording = false
    }

    private fun hasRecordPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }
}