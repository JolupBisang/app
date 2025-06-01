package com.imhungry.jjongseol.data.model.response

data class SocketResponse<T>(
    val type: SocketResponseType,
    val data: T
)



