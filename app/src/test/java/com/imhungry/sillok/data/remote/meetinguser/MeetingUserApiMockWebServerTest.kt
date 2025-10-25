package com.imhungry.sillok.data.remote.meetinguser

import com.google.gson.Gson
import com.imhungry.sillok.data.model.meetinguser.ParticipantAddReqDto
import com.imhungry.sillok.data.model.meetinguser.ParticipantAdditionResDto
import com.imhungry.sillok.data.model.meetinguser.ParticipantRemovalResDto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@OptIn(ExperimentalCoroutinesApi::class)
class MeetingUserApiMockWebServerTest {

    private lateinit var server: MockWebServer
    private lateinit var api: MeetingUserApi
    private val gson = Gson()

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MeetingUserApi::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun addMeetingUser_contract() = runTest {
        val res = gson.toJson(ParticipantAdditionResDto(meetingId = 10, addedCount = 1))
        server.enqueue(MockResponse().setResponseCode(200).setBody(res))

        val resp = api.addMeetingUser(10, ParticipantAddReqDto(listOf("a@a.com")))

        val req = server.takeRequest()
        assertEquals("/api/v1/meeting/10/participants", req.path)
        assertEquals("POST", req.method)
        assertTrue(resp.isSuccessful)
    }

    @Test
    fun removeMeetingUser_contract() = runTest {
        val res = gson.toJson(ParticipantRemovalResDto(meetingId = 10, participantId = 99))
        server.enqueue(MockResponse().setResponseCode(200).setBody(res))

        val resp = api.removeMeetingUser(10, 99)

        val req = server.takeRequest()
        assertEquals("/api/v1/meetings/10/participant/99", req.path)
        assertEquals("DELETE", req.method)
        assertTrue(resp.isSuccessful)
    }
}


