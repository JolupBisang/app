package com.imhungry.sillok.data.repository.meeting

import android.os.Build
import androidx.annotation.RequiresApi
import com.imhungry.sillok.data.model.meeting.MeetingRole
import com.imhungry.sillok.data.model.meeting.MeetingUpdateReqDto
import com.imhungry.sillok.data.model.meeting.TargetMeetingStatus
import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.model.agenda.Agenda
import com.imhungry.sillok.domain.model.meeting.AddTeamTagRequest
import com.imhungry.sillok.domain.model.meeting.CreateMeetingRequest
import com.imhungry.sillok.domain.model.meeting.DuplicatedMeeting
import com.imhungry.sillok.domain.model.meeting.Meeting
import com.imhungry.sillok.domain.model.meeting.MeetingDetailSummary
import com.imhungry.sillok.domain.model.meeting.MeetingSearchSlice
import com.imhungry.sillok.domain.model.meeting.MeetingStatus
import com.imhungry.sillok.domain.model.meeting.RemoveTeamTagRequest
import com.imhungry.sillok.domain.repository.meeting.MeetingRepository
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@RequiresApi(Build.VERSION_CODES.O)
class FakeMeetingRepository @Inject constructor() : MeetingRepository {

    companion object {
        // 더미 참가자 목록 (FakeUserRepository와 매칭)
        private val DUMMY_PARTICIPANTS = listOf(
            Meeting.Participant(
                userId = 1L,
                email = "joeungyeong23@gmail.com",
                role = MeetingRole.HOST
            ),
            Meeting.Participant(
                userId = 2L,
                email = "hong@example.com",
                role = MeetingRole.PARTICIPANT
            ),
            Meeting.Participant(
                userId = 3L,
                email = "kim@example.com",
                role = MeetingRole.PARTICIPANT
            ),
            Meeting.Participant(
                userId = 4L,
                email = "park@example.com",
                role = MeetingRole.PARTICIPANT
            )
        )

        // 더미 아젠다 목록
        private val DUMMY_AGENDAS = listOf(
            Agenda(agendaId = 1L, content = "프로젝트 목표와 범위 합의", isCompleted = true),
            Agenda(agendaId = 2L, content = "역할 분담 및 일정 수립", isCompleted = false),
            Agenda(agendaId = 3L, content = "기술 스택 및 아키텍처 논의", isCompleted = false),
            Agenda(agendaId = 4L, content = "다음 회의 일정 결정", isCompleted = false)
        )
    }

    override suspend fun createMeeting(request: CreateMeetingRequest): ApiResult<Long> {
        // 항상 ID 1L 을 반환하는 가짜 구현
        return ApiResult.Success(1L)
    }

    override suspend fun getMeetingDetail(meetingId: Long): ApiResult<Meeting> {
        // 현재 시간 기준으로 회의 시간 생성
        val now = LocalDateTime.now(ZoneId.of("Asia/Seoul"))
        val scheduledStartTime = now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val scheduledEndTime = now.plusHours(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val actualStartTime = now.minusMinutes(10).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

        val meeting = Meeting(
            meetingId = meetingId,
            title = "프로젝트 기획 회의",
            location = "서울시 강남구 테헤란로 123",
            scheduledStartTime = scheduledStartTime,
            actualStartTime = actualStartTime,
            scheduledEndTime = scheduledEndTime,
            targetTime = 5,
            restInterval = 1,
            restDuration = 1,
            meetingStatus = "IN_PROGRESS",
            participants = DUMMY_PARTICIPANTS,
            agendas = DUMMY_AGENDAS,
            teamNames = listOf("개발팀", "디자인팀"),
            isHost = true // 현재 사용자가 호스트
        )

        return ApiResult.Success(meeting)
    }

    override suspend fun getMeetings(
        year: Int?,
        month: Int?,
        title: String?,
        page: Int,
        size: Int
    ): ApiResult<MeetingSearchSlice> {
        TODO("Not yet implemented")
    }

    override suspend fun getMeetings2(
        year: Int,
        month: Int
    ): ApiResult<List<MeetingDetailSummary>> {
        // 오늘 날짜로 더미 회의 생성
        val now = LocalDateTime.now(ZoneId.of("Asia/Seoul"))
        // 오늘 날짜가 요청한 년/월과 일치하는 경우에만 반환
        if (now.year == year && now.monthValue == month) {
            val scheduledStartTime = now.withHour(14).withMinute(0).withSecond(0).withNano(0)
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

            val meeting = MeetingDetailSummary(
                id = 1002L,
                title = "프로젝트 기획 회의",
                scheduledStartTime = scheduledStartTime,
                targetTime = 60,
                status = "IN_PROGRESS"
            )

            return ApiResult.Success(listOf(meeting))
        }

        // 요청한 년/월이 오늘과 다르면 빈 리스트 반환
        return ApiResult.Success(emptyList())
    }

    override suspend fun updateMeetingStatus(
        meetingId: Long,
        targetStatus: TargetMeetingStatus
    ): ApiResult<Unit> {
        // 항상 성공
        return ApiResult.Success(Unit)
    }

    override suspend fun updateMeeting(
        meetingId: Long,
        request: MeetingUpdateReqDto
    ): ApiResult<Unit> {
        // 항상 성공
        return ApiResult.Success(Unit)
    }

    override suspend fun checkDuplicatedTime(
        startTime: String,
        targetMinutes: Long
    ): ApiResult<List<DuplicatedMeeting>> {
        // 항상 중복 없음
        return ApiResult.Success(emptyList())
    }

    override suspend fun addTeamTag(
        meetingId: Long,
        request: AddTeamTagRequest
    ): ApiResult<Unit> {
        // 항상 성공
        return ApiResult.Success(Unit)
    }

    override suspend fun removeTeamTag(
        meetingId: Long,
        request: RemoveTeamTagRequest
    ): ApiResult<Unit> {
        // 항상 성공
        return ApiResult.Success(Unit)
    }

    override suspend fun searchMeetings(
        title: String,
        page: Int,
        size: Int
    ): ApiResult<MeetingSearchSlice> {
        // 더미 검색 결과 생성
        val now = LocalDateTime.now(ZoneId.of("Asia/Seoul"))
        val dummyMeetings = (0 until size).map { index ->
            val meetingIndex = page * size + index
            val scheduledStartTime = now.plusDays(meetingIndex.toLong())
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

            MeetingDetailSummary(
                id = meetingIndex.toLong() + 1,
                title = if (title.isNotBlank()) "$title - 회의 ${meetingIndex + 1}" else "회의 ${meetingIndex + 1}",
                scheduledStartTime = scheduledStartTime,
                targetTime = 60,
                status = if (meetingIndex % 3 == 0) "IN_PROGRESS" else "WAITING"
            )
        }

        val totalElements = 20 // 총 20개의 더미 회의
        val startIndex = page * size
        val endIndex = minOf(startIndex + size, totalElements)
        val hasNext = endIndex < totalElements
        val hasPrevious = page > 0

        val slice = MeetingSearchSlice(
            content = dummyMeetings,
            number = page,
            size = size,
            numberOfElements = dummyMeetings.size,
            first = page == 0,
            last = !hasNext,
            hasNext = hasNext,
            hasPrevious = hasPrevious
        )

        return ApiResult.Success(slice)
    }
}
