package com.imhungry.sillok.data.repository.meeting

import com.imhungry.sillok.data.mapper.meeting.MeetingMapper
import com.imhungry.sillok.data.model.meeting.*
import com.imhungry.sillok.data.remote.meeting.MeetingApi
import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.model.meeting.CreateMeetingRequest
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class MeetingRepositoryImplTest {

    private val api: MeetingApi = mockk()
    private val mapper = MeetingMapper()
    private val repository = MeetingRepositoryImpl(api, mapper)

    @Test
    fun createMeeting_success_returnsId() = runTest {
        val req = CreateMeetingRequest(
            title = "t", location = "l", scheduledStartTime = "now",
            targetTime = 60, restInterval = 30, restDuration = 10,
            participants = emptyList(), agendas = emptyList()
        )
        coEvery { api.createMeeting(any()) } returns Response.success(MeetingCreationResDto(123))

        val result = repository.createMeeting(req)

        assertTrue(result is ApiResult.Success)
        assertEquals(123L, (result as ApiResult.Success).data)
    }

    @Test
    fun updateMeetingStatus_success() = runTest {
        coEvery { api.updateMeetingStatus(10, MeetingStatusUpdateReqDto("STARTED")) } returns Response.success(
            MeetingStatusChangeResDto(meetingId = 10, finalStatus = "STARTED")
        )

        val result = repository.updateMeetingStatus(10, "STARTED")

        assertTrue(result is ApiResult.Success)
    }
}


