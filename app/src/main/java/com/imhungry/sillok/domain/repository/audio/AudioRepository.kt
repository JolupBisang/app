package com.imhungry.sillok.domain.repository.audio

import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.model.audio.AudioInfo
import java.io.File

interface AudioRepository {
    suspend fun embeddingAudio(audioFile: File): ApiResult<Unit>
    suspend fun getAudioList(meetingId: Long): ApiResult<List<AudioInfo>>
}