package com.imhungry.jjongseol.data.repository

import com.imhungry.jjongseol.data.model.audio.response.AudioListResponse
import com.imhungry.jjongseol.data.network.api.AudioApi
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import javax.inject.Inject

sealed class AudioResult<out T> {
    data class Success<T>(val data: T) : AudioResult<T>()
    data class Error(val message: String) : AudioResult<Nothing>()
    data class Exception(val throwable: Throwable) : AudioResult<Nothing>()
}

class AudioRepository @Inject constructor(
    private val audioApi: AudioApi
) {
    suspend fun uploadAudio(file: File): AudioResult<Unit> {
        return try {
            val requestFile = RequestBody.create(
                "audio/mp4".toMediaTypeOrNull(),
                file
            )
            val body = MultipartBody.Part.createFormData(
                "audioFile",
                file.name,
                requestFile
            )

            val response = audioApi.embeddingAudio(body)
            if (response.isSuccessful) {
                AudioResult.Success(Unit)
            } else {
                AudioResult.Error("업로드 실패: ${response.code()}")
            }
        } catch (e: Exception) {
            AudioResult.Exception(e)
        }
    }

    suspend fun getAudioList(meetingId: Long): AudioResult<AudioListResponse> {
        return try {
            val response = audioApi.getAudioList(meetingId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.data != null) {
                    AudioResult.Success(body.data)
                } else {
                    AudioResult.Error("데이터 없음")
                }
            } else {
                AudioResult.Error("오디오 목록 조회 실패: ${response.code()}")
            }
        } catch (e: Exception) {
            AudioResult.Exception(e)
        }
    }
}
