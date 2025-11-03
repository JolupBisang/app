package com.imhungry.sillok.data.model.realtime

data class SocketResponse<T>(
    val type: SocketResponseType,
    val data: T?
)