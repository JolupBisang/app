package com.imhungry.sillok.data.repository.meetinguser

import com.imhungry.sillok.data.model.meetinguser.ParticipantAddReqDto
import com.imhungry.sillok.data.model.meetinguser.ParticipantAdditionResDto
import com.imhungry.sillok.data.model.meetinguser.ParticipantRemovalResDto
import com.imhungry.sillok.data.remote.meetinguser.MeetingUserApi
import com.imhungry.sillok.data.util.ApiResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class MeetingUserRepositoryImplTest {

    private val api: MeetingUserApi = mockk()
    private val repository = MeetingUserRepositoryImpl(api)

    @Test
    fun addMeetingUser_success() = runTest {
        coEvery { api.addMeetingUser(10, ParticipantAddReqDto(listOf("a@a.com"))) } returns Response.success(
            ParticipantAdditionResDto(meetingId = 10, addedCount = 1)
        )

        val result = repository.addMeetingUser(10, listOf("a@a.com"))
        assertTrue(result is ApiResult.Success)
    }

    @Test
    fun removeMeetingUser_success() = runTest {
        coEvery { api.removeMeetingUser(10, 99) } returns Response.success(
            ParticipantRemovalResDto(meetingId = 10, participantId = 99)
        )

        val result = repository.removeMeetingUser(10, 99)
        assertTrue(result is ApiResult.Success)
    }
}


