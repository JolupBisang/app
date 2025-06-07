package com.imhungry.jjongseol.data.repository

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object ErrorEventRepository {
    private val _errorEvents = MutableSharedFlow<String>(extraBufferCapacity = 10)
    val errorEvents = _errorEvents.asSharedFlow()
    fun emitError(msg: String) {
        _errorEvents.tryEmit(msg)
    }
}