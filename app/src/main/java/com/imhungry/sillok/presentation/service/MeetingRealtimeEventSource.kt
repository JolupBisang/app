package com.imhungry.sillok.presentation.service

import kotlinx.coroutines.flow.Flow

/**
 * 회의 진행 중 WebSocket / SSE / Service 에서 오는 이벤트들을
 * ViewModel 에게 전달하기 위한 추상 인터페이스.
 *
 * - 실제 앱: RealMeetingRealtimeEventSource (Service + WebSocket + SSE)
 * - 디버그: FakeMeetingRealtimeEventSource (서버 없이 가짜 이벤트 발생)
 */
interface MeetingRealtimeEventSource {

    /** ServiceEvent 스트림 (WebSocket/SSE에서 올라오는 이벤트) */
    val events: Flow<ServiceEvent>

    /**
     * 회의 실시간 처리를 시작한다.
     *
     * @param serverUrl   서버 BASE URL (ex. BuildConfig.BASE_URL)
     * @param meetingId   회의 ID
     * @param jwtToken    JWT Access Token
     */
    suspend fun start(
        serverUrl: String,
        meetingId: Long,
        jwtToken: String
    )

    /**
     * 모든 연결을 해제하고 Service 를 중지한다.
     */
    suspend fun stop()

    /**
     * 마이크 on/off 토글 (실제 구현은 Service 에 ACTION_TOGGLE_MIC 전달)
     */
    fun toggleMic()
}
