package com.imhungry.jjongseol.viewmodel

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.imhungry.jjongseol.data.audio.AudioRecorder
import com.imhungry.jjongseol.util.buildPacket
import com.imhungry.jjongseol.util.convertPcmToOpus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class MeetingViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val context by lazy { application.applicationContext }

    private lateinit var audioRecorder: AudioRecorder
    private lateinit var pcmFile: File
    private lateinit var opusFile: File

    //private val webSocketManager = WebSocketManager()

    @RequiresApi(Build.VERSION_CODES.O)
    fun startRecordingAndStreaming() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 파일 경로 준비
                pcmFile = File(context.cacheDir, "recording.pcm")
                opusFile = File(context.cacheDir, "recording.opus")
                Log.d("MeetingViewModel", "녹음 파일 위치: ${pcmFile.absolutePath}")

                // WebSocket 연결
                /*webSocketManager.connect(
                    url = "",
                    onMessage = { Log.d("WebSocket", "서버 응답: $it") },
                    onFailure = { Log.e("WebSocket", "연결 실패", it) }
                )*/

                // 녹음 준비 및 시작
                audioRecorder = AudioRecorder(context, pcmFile)

                val recordingJob = launch {
                    audioRecorder.startRecording()
                }
                Log.d("Audio", "녹음 시작됨")

                // 5초간 대기 후 중지
                delay(5000)
                audioRecorder.stopRecording()
                Log.d("Audio", "녹음 중지 호출됨")

                // 녹음 종료까지 대기
                recordingJob.join()
                Log.d("Audio", "녹음 완료됨")

                // Opus 변환
                convertPcmToOpus(pcmFile.absolutePath, opusFile.absolutePath)
                Log.d("Audio", "Opus 변환 완료")

                // 패킷
                val opusBytes = opusFile.readBytes()
                val finalPacket = buildPacket(opusBytes, 1, 1, 1)

                // 전송
                /*webSocketManager.sendBinary(finalPacket)
                Log.d("Audio", "WebSocket 전송 완료")*/

                // 8. 종료
                /*webSocketManager.close()
                Log.d("Audio", "WebSocket 연결 종료")*/

            } catch (e: Exception) {
                Log.e("Audio", "오디오 전송 실패", e)
            }
        }
    }
}
