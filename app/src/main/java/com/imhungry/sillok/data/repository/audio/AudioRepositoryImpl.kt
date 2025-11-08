package com.imhungry.sillok.data.repository.audio

import com.imhungry.sillok.data.mapper.audio.AudioMapper
import com.imhungry.sillok.data.remote.audio.AudioApi
import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.model.audio.AudioInfo
import com.imhungry.sillok.domain.repository.audio.AudioRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import javax.inject.Inject

class AudioRepositoryImpl @Inject constructor(
    private val api: AudioApi,
    private val mapper: AudioMapper
) : AudioRepository {

    override suspend fun embeddingAudio(audioFile: java.io.File): ApiResult<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val requestFile = audioFile.asRequestBody("audio/mp4".toMediaTypeOrNull())
                val multipart =
                    MultipartBody.Part.createFormData("audioFile", audioFile.name, requestFile)
                val res = api.embeddingAudio(multipart)

                if (res.isSuccessful) {
                    ApiResult.Success(Unit)
                } else {
                    val errorBody = res.errorBody()?.string()
                    ApiResult.Failure(errorBody ?: res.message())
                }
            } catch (e: Exception) {
                ApiResult.Failure(e.localizedMessage ?: "알 수 없는 오류")
            }
        }

    override suspend fun getAudioList(meetingId: Long): ApiResult<List<AudioInfo>> =
        withContext(Dispatchers.IO) {
            try {
                val res = api.getAudioList(meetingId)
                if (res.isSuccessful) {
                    val dto = res.body()
                    if (dto != null) {
                        val list = dto.audioList.map { mapper.toDomain(it) }
                        ApiResult.Success(list)
                    } else {
                        ApiResult.Failure("응답 파싱 오류")
                    }
                } else {
                    ApiResult.Failure(res.message())
                }
            } catch (e: Exception) {
                ApiResult.Failure(e.localizedMessage ?: "알 수 없는 오류")
            }
        }
}
