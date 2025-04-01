package com.imhungry.jjongseol.util

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import org.json.JSONObject
import java.nio.ByteBuffer
import java.time.LocalDateTime

@RequiresApi(Build.VERSION_CODES.O)
fun buildPacket(
    opusBytes: ByteArray,
    userId: Long,
    meetingId: Long,
    chunkId: Int
): ByteArray {
    val metadata = JSONObject().apply {
        put("type", "audio")
        put("userId", userId)
        put("meetingId", meetingId)
        put("chunkId", chunkId)
        put("timestamp", LocalDateTime.now().toString())
        put("encoding", "opus")
    }

    val jsonBytes = metadata.toString().toByteArray(Charsets.UTF_8)

    val buffer = ByteBuffer.allocate(4 + jsonBytes.size + opusBytes.size)
    buffer.putInt(jsonBytes.size)
    buffer.put(jsonBytes)
    buffer.put(opusBytes)

    return buffer.array()
}