package com.imhungry.sillok.data.remote.feedback

import com.google.gson.Gson
import com.imhungry.sillok.data.model.feedback.FeedbackListResDto
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
class FeedbackApiMockWebServerTest {

    private lateinit var server: MockWebServer
    private lateinit var api: FeedbackApi
    private val gson = Gson()

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FeedbackApi::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun getFeedbacks_contract() = runTest {
        val body = gson.toJson(FeedbackListResDto(id=1, comment="c", timestamp="t"))
        server.enqueue(MockResponse().setResponseCode(200).setBody(body))

        val resp = api.getFeedbacks(10)

        val req = server.takeRequest()
        assertEquals("/api/v1/meetings/10/feedbacks", req.path)
        assertEquals("GET", req.method)
        assertTrue(resp.isSuccessful)
        assertEquals("c", resp.body()!!.comment)
    }
}


