package com.imhungry.sillok.di

import android.content.Context
import com.imhungry.sillok.data.local.TokenStore
import com.imhungry.sillok.data.remote.realtime.AudioPacketStore
import com.imhungry.sillok.data.remote.realtime.FileAudioPacketStore
import com.imhungry.sillok.data.remote.realtime.RealtimeClient
import com.imhungry.sillok.data.remote.realtime.RealtimeEventBus
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object RealtimeModule {

    @Provides
    @Singleton
    fun provideAudioPacketStore(
        @ApplicationContext appContext: Context
    ): AudioPacketStore = FileAudioPacketStore(appContext)

    @Provides
    @Singleton
    fun provideRealtimeClientCreator(
        okHttpClient: OkHttpClient,
        @Named("sse") sseOkHttpClient: OkHttpClient,
        tokenStore: TokenStore,
        eventBus: RealtimeEventBus,
        packetStore: AudioPacketStore,
    ): (CoroutineScope) -> RealtimeClient {
        return { scope: CoroutineScope ->
            RealtimeClient(okHttpClient, sseOkHttpClient, tokenStore, eventBus, packetStore, scope).also { client ->
                // No-op: event bus is provided for future wiring if needed
            }
        }
    }
}


