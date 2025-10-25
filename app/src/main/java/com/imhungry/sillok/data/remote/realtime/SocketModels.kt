// 웹소켓 메시지 타입과 페이로드(Envelope) 모델 및 파싱 유틸
package com.imhungry.sillok.data.remote.realtime

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

// 서버가 전송하는 소켓 이벤트 타입을 열거형으로 정의합니다.
enum class SocketType {
    // 연결 수립 알림(서버 기준 마지막 처리 chunkId 등 메타)
    CONNECTION_ESTABLISHED,
    // 에러 알림
    ERROR,
    // 회의 종료 알림
    MEETING_COMPLETED,
    // 화자 분리된 세그먼트 도착
    DIARIZED_SEGMENT,
    // 회의록 생성 완료
    MEETING_NOTE_CREATED,
    // 녹음 산출물 생성 완료
    MEETING_RECORD_MADED,
    // 아젠다 갱신 알림
    AGENDA_UPDATED,
    // 정의되지 않은 타입
    UNKNOWN;

    companion object {
        // 서버의 문자열 타입을 안전하게 enum으로 매핑합니다.
        fun from(raw: String?): SocketType = when (raw) {
            "CONNECTION_ESTABLISHED" -> CONNECTION_ESTABLISHED
            "ERROR" -> ERROR
            "MEETING_COMPLETED" -> MEETING_COMPLETED
            "DIARIZED_SEGMENT" -> DIARIZED_SEGMENT
            "MEETING_NOTE_CREATED" -> MEETING_NOTE_CREATED
            "MEETING_RECORD_MADED" -> MEETING_RECORD_MADED
            "AGENDA_UPDATED" -> AGENDA_UPDATED
            else -> UNKNOWN
        }
    }
}

// 소켓 메시지의 공통 래퍼로, 타입과 실제 데이터(JSON)를 포함합니다.
data class SocketEnvelope(
    @SerializedName("type") val typeRaw: String?,
    @SerializedName("data") val data: JsonElement?
) {
    // 파싱된 타입(enum)을 지연 계산하여 제공합니다.
    val type: SocketType get() = SocketType.from(typeRaw)

    // 데이터 필드를 제네릭 타입으로 안전하게 역직렬화합니다.
    inline fun <reified T> dataAs(): T? {
        return try {
            if (data == null || data.isJsonNull) return null
            Gson().fromJson(data, T::class.java)
        } catch (_: Throwable) { null }
    }

    // 원본 JSON 문자열을 그대로 반환합니다(디버깅/로깅용).
    fun rawDataJson(): String = data?.toString().orEmpty()
}

// CONNECTION_ESTABLISHED 데이터 모델(마지막 처리 chunkId/회의 시작 시간)
data class ConnectionEstablishedData(
    @SerializedName("lastProcessedChunkId") val lastProcessedChunkId: Int?,
    @SerializedName("meetingStartTime") val meetingStartTime: String?,
)

// ERROR 데이터 모델(메시지)
data class ErrorData(
    @SerializedName("message") val message: String?,
)


