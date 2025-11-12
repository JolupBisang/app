package com.imhungry.sillok.di

import com.imhungry.sillok.BuildConfig
import com.imhungry.sillok.data.remote.agenda.AgendaApi
import com.imhungry.sillok.data.remote.audio.AudioApi
import com.imhungry.sillok.data.remote.feedback.FeedbackApi
import com.imhungry.sillok.data.remote.interceptor.AuthInterceptor
import com.imhungry.sillok.data.remote.meeting.MeetingApi
import com.imhungry.sillok.data.remote.meetinguser.MeetingUserApi
import com.imhungry.sillok.data.remote.participation.ParticipationRateApi
import com.imhungry.sillok.data.remote.places.PlacesApi
import com.imhungry.sillok.data.remote.segment.SegmentApi
import com.imhungry.sillok.data.remote.summary.SummaryApi
import com.imhungry.sillok.data.remote.team.TeamApi
import com.imhungry.sillok.data.remote.user.UserApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    @Named("sse")
    fun provideSseOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .readTimeout(15, TimeUnit.MINUTES)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
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
    fun provideFeedbackApi(retrofit: Retrofit): FeedbackApi {
        return retrofit.create(FeedbackApi::class.java)
    }

    @Provides
    @Singleton
    fun provideSummaryApi(retrofit: Retrofit): SummaryApi {
        return retrofit.create(SummaryApi::class.java)
    }

    @Provides
    @Singleton
    fun provideSegmentApi(retrofit: Retrofit): SegmentApi {
        return retrofit.create(SegmentApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAgendaApi(retrofit: Retrofit): AgendaApi {
        return retrofit.create(AgendaApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAudioApi(retrofit: Retrofit): AudioApi {
        return retrofit.create(AudioApi::class.java)
    }

    @Provides
    @Singleton
    fun provideParticipationApi(retrofit: Retrofit): ParticipationRateApi {
        return retrofit.create(ParticipationRateApi::class.java)
    }

    @Provides
    @Singleton
    fun provideMeetingUserApi(retrofit: Retrofit): MeetingUserApi {
        return retrofit.create(MeetingUserApi::class.java)
    }

    @Provides
    @Singleton
    fun provideTeamApi(retrofit: Retrofit): TeamApi {
        return retrofit.create(TeamApi::class.java)
    }

    @Provides
    @Singleton
    fun providePlacesApi(): PlacesApi {
        return Retrofit.Builder()
            .baseUrl("https://openapi.naver.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PlacesApi::class.java)
    }
} 