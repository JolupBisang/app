package com.imhungry.jjongseol.data.model.response

data class SocketResponse(
    val type: SocketResponseType,
    val data: Any
)

enum class SocketResponseType {
    LAST_PROCESSED_CHUNK_ID,
    ERROR
}
