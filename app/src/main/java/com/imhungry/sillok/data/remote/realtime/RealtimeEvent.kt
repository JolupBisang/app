// SSE/웹소켓에서 발생하는 실시간 이벤트를 표현하는 sealed 클래스 모음
package com.imhungry.sillok.data.remote.realtime

// 실시간 채널(SSE/WS)에서 발생하는 모든 이벤트의 상위 타입으로, UI와 서비스 간 계약을 표준화합니다.
sealed class RealtimeEvent {
    // 실시간 요약 텍스트가 생성되었음을 나타내는 이벤트입니다.
    data class Summary(
        val timestamp: String,
        val summary: String,
    ) : RealtimeEvent()

    // 사용자 피드백(알림/코멘트)이 수신되었음을 나타내는 이벤트입니다.
    data class Feedback(
        val timestamp: String,
        val comment: String,
    ) : RealtimeEvent()

    // 사용자별 발언/참여 비율이 갱신되었음을 나타내는 이벤트입니다.
    data class ParticipationRate(
        val timestamp: String,
        val participationRates: List<Item>,
    ) : RealtimeEvent() {
        // 개별 사용자 참여 비율 항목을 표현합니다.
        data class Item(
            val userId: Long,
            val rate: Double,
        )
    }

    // WebSocket events
    // 서버와의 오디오 소켓 연결이 준비되었고, 서버가 마지막 처리한 chunkId를 고지했음을 의미합니다.
    data class ConnectionEstablished(
        val lastProcessedChunkId: Int,
        val meetingStartTime: String?
    ) : RealtimeEvent()

    // 소켓에서 에러 메시지를 수신한 경우입니다.
    data class SocketError(
        val message: String
    ) : RealtimeEvent()

    // 회의가 서버 측에서 종료 처리되었음을 알리는 이벤트입니다.
    data class MeetingCompleted(
        val meetingId: Long?
    ) : RealtimeEvent()

    // 스피커 분리된(diarization) 대화 세그먼트가 도착했음을 나타냅니다(원본 JSON 보관).
    data class DiarizedSegment(
        val payloadJson: String
    ) : RealtimeEvent()

    // 회의록(노트)이 생성 완료되었음을 의미합니다.
    object MeetingNoteCreated : RealtimeEvent()

    // 회의 녹음 산출물이 생성되었음을 의미합니다.
    object MeetingRecordMaded : RealtimeEvent()

    // 아젠다가 업데이트되었음을 알리는 이벤트(원본 JSON 보관).
    data class AgendaUpdated(
        val payloadJson: String
    ) : RealtimeEvent()
}


