package com.imhungry.jjongseol.data.model.audio.response

import com.imhungry.jjongseol.data.model.audio.dto.AudioInfo

data class AudioListResponse(
    val audioList: List<AudioInfo>
)