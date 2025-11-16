package com.imhungry.sillok.di

import com.imhungry.sillok.presentation.service.MeetingRealtimeEventSource
import com.imhungry.sillok.presentation.service.RealMeetingRealtimeEventSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 실시간 회의 이벤트 소스를 Hilt 로 주입하기 위한 모듈.
 *
 * - main(src/main): RealMeetingRealtimeEventSource 를 사용
 * - debug(src/debug): 같은 인터페이스를 FakeMeetingRealtimeEventSource 로 override 할 예정
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RealtimeModule {

    @Binds
    @Singleton
    abstract fun bindMeetingRealtimeEventSource(
        impl: RealMeetingRealtimeEventSource
    ): MeetingRealtimeEventSource
}
