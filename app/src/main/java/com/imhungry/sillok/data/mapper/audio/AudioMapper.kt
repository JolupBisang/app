package com.imhungry.sillok.data.mapper.audio

import com.imhungry.sillok.data.model.audio.AudioInfoDto
import com.imhungry.sillok.domain.model.audio.AudioInfo
import javax.inject.Inject

class AudioMapper @Inject constructor() {
    fun toDomain(dto: AudioInfoDto): AudioInfo {
        return AudioInfo(
            userId = dto.userId,
            presignedUrl = dto.presignedUrl
        )
    }
}