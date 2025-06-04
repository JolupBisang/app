package com.imhungry.jjongseol.data.repository

import com.imhungry.jjongseol.data.model.chat.DiarizedSegment
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

object DiarizedSegmentRepository {
    private val _diarizedSegmentFlow = MutableSharedFlow<DiarizedSegment>(extraBufferCapacity = 64)
    val diarizedSegmentFlow: SharedFlow<DiarizedSegment> = _diarizedSegmentFlow

    suspend fun emit(msg: DiarizedSegment) {
        _diarizedSegmentFlow.emit(msg)
    }
}
