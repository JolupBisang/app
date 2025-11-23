package com.imhungry.sillok.di

import com.imhungry.sillok.presentation.service.FakeMeetingRealtimeEventSource
import com.imhungry.sillok.presentation.service.MeetingRealtimeEventSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RealtimeModule {

    @Binds
    @Singleton
    abstract fun bindMeetingRealtimeEventSource(
        impl: FakeMeetingRealtimeEventSource
    ): MeetingRealtimeEventSource
}
