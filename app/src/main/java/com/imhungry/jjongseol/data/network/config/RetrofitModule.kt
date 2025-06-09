package com.imhungry.jjongseol.data.network.config

import com.imhungry.jjongseol.BuildConfig
import com.imhungry.jjongseol.data.network.api.AgendaApi
import com.imhungry.jjongseol.data.network.api.AudioApi
import com.imhungry.jjongseol.data.network.api.FeedbackApi
import com.imhungry.jjongseol.data.network.api.MeetingApi
import com.imhungry.jjongseol.data.network.api.MeetingUserApi
import com.imhungry.jjongseol.data.network.api.ParticipationRateApi
import com.imhungry.jjongseol.data.network.api.SegmentApi
import com.imhungry.jjongseol.data.network.api.SummaryApi
import com.imhungry.jjongseol.data.network.api.UserApi
import com.imhungry.jjongseol.data.repository.AudioRepository
import com.imhungry.jjongseol.data.repository.FeedbackRepository
import com.imhungry.jjongseol.data.repository.ParticipationRateRepository
import com.imhungry.jjongseol.data.repository.SegmentRepository
import com.imhungry.jjongseol.data.repository.SummaryRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RetrofitModule {

    private const val BASE_URL = BuildConfig.BASE_URL

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAgendaApi(retrofit: Retrofit): AgendaApi {
        return retrofit.create(AgendaApi::class.java)
    }

    @Provides
    @Singleton
    fun provideMeetingApi(retrofit: Retrofit): MeetingApi {
        return retrofit.create(MeetingApi::class.java)
    }

    @Provides
    @Singleton
    fun provideUserApi(retrofit: Retrofit): UserApi {
        return retrofit.create(UserApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAudioApi(retrofit: Retrofit): AudioApi {
        return retrofit.create(AudioApi::class.java)
    }

    @Provides
    @Singleton
    fun provideMeetingUserApi(retrofit: Retrofit): MeetingUserApi {
        return retrofit.create(MeetingUserApi::class.java)
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface MeetingUserApiEntryPoint {
        fun meetingUserApi(): MeetingUserApi
    }

    @Provides
    @Singleton
    fun provideSegmentApi(retrofit: Retrofit): SegmentApi {
        return retrofit.create(SegmentApi::class.java)
    }

    @Provides
    @Singleton
    fun provideSegmentRepository(segmentApi: SegmentApi): SegmentRepository =
        SegmentRepository(segmentApi)

    @Provides
    @Singleton
    fun provideAudioRepository(audioApi: AudioApi): AudioRepository {
        return AudioRepository(audioApi)
    }

    @Provides
    @Singleton
    fun provideSummaryApi(retrofit: Retrofit): SummaryApi {
        return retrofit.create(SummaryApi::class.java)
    }

    @Provides
    @Singleton
    fun provideSummaryRepository(summaryApi: SummaryApi): SummaryRepository =
        SummaryRepository(summaryApi)

    @Provides
    @Singleton
    fun provideParticipationRateApi(retrofit: Retrofit): ParticipationRateApi {
        return retrofit.create(ParticipationRateApi::class.java)
    }

    @Provides
    @Singleton
    fun provideParticipationRateRepository(api: ParticipationRateApi): ParticipationRateRepository =
        ParticipationRateRepository(api)

    @Provides
    @Singleton
    fun provideFeedbackApi(retrofit: Retrofit): FeedbackApi {
        return retrofit.create(FeedbackApi::class.java)
    }

    @Provides
    @Singleton
    fun provideFeedbackRepository(feedbackApi: FeedbackApi): FeedbackRepository =
        FeedbackRepository(feedbackApi)
}