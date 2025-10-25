package com.imhungry.sillok.data.remote.meeting

import com.google.gson.Gson
import com.imhungry.sillok.data.model.meeting.*
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
class MeetingApiMockWebServerTest {

    private lateinit var server: MockWebServer
    private lateinit var api: MeetingApi
    private val gson = Gson()

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MeetingApi::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun createMeeting_contract() = runTest {
        val res = gson.toJson(MeetingCreationResDto(meetingId = 123))
        server.enqueue(MockResponse().setResponseCode(200).setBody(res))

        val resp = api.createMeeting(MeetingReqDto("t","l","now",60,30,10, emptyList(), emptyList()))

        val req = server.takeRequest()
        assertEquals("/api/v1/meeting", req.path)
        assertEquals("POST", req.method)
        assertTrue(resp.isSuccessful)
        assertEquals(123L, resp.body()!!.meetingId)
    }
}


