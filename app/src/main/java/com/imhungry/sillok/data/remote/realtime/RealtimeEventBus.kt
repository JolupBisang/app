// 앱 전역으로 실시간 이벤트를 브로드캐스트하는 경량 이벤트 버스
package com.imhungry.sillok.data.remote.realtime

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Singleton

// 싱글톤으로 앱 전역에서 사용될 이벤트 버스임을 나타냅니다.
@Singleton
// 실시간 이벤트를 발행/구독하기 위한 경량 이벤트 버스 클래스를 정의합니다.
class RealtimeEventBus @Inject constructor() {
    // 버퍼 용량 64로 설정된 SharedFlow로 역압력 상황에서도 일부 버퍼링이 가능하게 합니다.
    private val _events = MutableSharedFlow<RealtimeEvent>(extraBufferCapacity = 64)
    // 외부에 읽기 전용 SharedFlow를 노출하여 발행은 내부에서만 가능하도록 캡슐화합니다.
    val events: SharedFlow<RealtimeEvent> = _events

    // 이벤트를 안전하게(코루틴 컨텍스트 없이) 시도 발행하는 헬퍼 메서드입니다.
    fun tryEmit(event: RealtimeEvent) {
        // 구독자가 없거나 버퍼가 가득 차도 실패할 수 있으나, 예외를 던지지 않습니다.
        _events.tryEmit(event)
    }
}


