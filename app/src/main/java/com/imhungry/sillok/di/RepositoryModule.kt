package com.imhungry.sillok.di

import com.imhungry.sillok.data.repository.agenda.AgendaRepositoryImpl
import com.imhungry.sillok.data.repository.audio.AudioRepositoryImpl
import com.imhungry.sillok.data.repository.feedback.FeedbackRepositoryImpl
import com.imhungry.sillok.data.repository.folder.MeetingFolderRepositoryImpl
import com.imhungry.sillok.data.repository.meeting.MeetingRepositoryImpl
import com.imhungry.sillok.data.repository.meetinguser.MeetingUserRepositoryImpl
import com.imhungry.sillok.data.repository.participation.ParticipationRateRepositoryImpl
import com.imhungry.sillok.data.repository.places.PlacesRepositoryImpl
import com.imhungry.sillok.data.repository.segment.SegmentRepositoryImpl
import com.imhungry.sillok.data.repository.summary.SummaryRepositoryImpl
import com.imhungry.sillok.data.repository.team.TeamRepositoryImpl
import com.imhungry.sillok.data.repository.user.UserRepositoryImpl
import com.imhungry.sillok.domain.repository.agenda.AgendaRepository
import com.imhungry.sillok.domain.repository.audio.AudioRepository
import com.imhungry.sillok.domain.repository.feedback.FeedbackRepository
import com.imhungry.sillok.domain.repository.meeting.MeetingRepository
import com.imhungry.sillok.domain.repository.folder.MeetingMinutesFolderRepository
import com.imhungry.sillok.domain.repository.meetinguser.MeetingUserRepository
import com.imhungry.sillok.domain.repository.participation.ParticipationRateRepository
import com.imhungry.sillok.domain.repository.places.PlacesRepository
import com.imhungry.sillok.domain.repository.segment.SegmentRepository
import com.imhungry.sillok.domain.repository.summary.SummaryRepository
import com.imhungry.sillok.domain.repository.team.TeamRepository
import com.imhungry.sillok.domain.repository.user.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMeetingRepository(
        meetingRepositoryImpl: MeetingRepositoryImpl
    ): MeetingRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindFeedbackRepository(
        feedbackRepositoryImpl: FeedbackRepositoryImpl
    ): FeedbackRepository

    @Binds
    @Singleton
    abstract fun bindSummaryRepository(
        summaryRepositoryImpl: SummaryRepositoryImpl
    ): SummaryRepository

    @Binds
    @Singleton
    abstract fun bindSegmentRepository(
        segmentRepositoryImpl: SegmentRepositoryImpl
    ): SegmentRepository

    @Binds
    @Singleton
    abstract fun bindAgendaRepository(
        agendaRepositoryImpl: AgendaRepositoryImpl
    ): AgendaRepository

    @Binds
    @Singleton
    abstract fun bindAudioRepository(
        audioRepositoryImpl: AudioRepositoryImpl
    ): AudioRepository

    @Binds
    @Singleton
    abstract fun bindParticipationRepository(
        participationRepositoryImpl: ParticipationRateRepositoryImpl
    ): ParticipationRateRepository

    @Binds
    @Singleton
    abstract fun bindMeetingUserRepository(
        meetingUserRepositoryImpl: MeetingUserRepositoryImpl
    ): MeetingUserRepository

    @Binds
    abstract fun bindPlacesRepository(
        placesRepositoryImpl: PlacesRepositoryImpl
    ): PlacesRepository

    @Binds
    @Singleton
    abstract fun bindTeamRepository(
        teamRepositoryImpl: TeamRepositoryImpl
    ): TeamRepository

    @Binds
    @Singleton
    abstract fun bindMeetingMinutesFolderRepository(
        meetingFolderRepositoryImpl: MeetingFolderRepositoryImpl
    ): MeetingMinutesFolderRepository
} 