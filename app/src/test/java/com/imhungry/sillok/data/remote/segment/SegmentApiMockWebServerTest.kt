package com.imhungry.sillok.data.remote.segment

import com.google.gson.Gson
import com.imhungry.sillok.data.model.segment.SegmentListResDto
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
class SegmentApiMockWebServerTest {

    private lateinit var server: MockWebServer
    private lateinit var api: SegmentApi
    private val gson = Gson()

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SegmentApi::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun getSegments_contract() = runTest {
        val body = gson.toJson(
            SegmentListResDto(
                id = 1, userId = 2,
                segmentOrder = 1, timestamp = "t", text = "hello", lang = "ko"
            )
        )
        server.enqueue(MockResponse().setResponseCode(200).setBody(body))

        val resp = api.getSegments(10)

        val req = server.takeRequest()
        assertEquals("/api/v1/meeting/10/segments", req.path)
        assertEquals("GET", req.method)
        assertTrue(resp.isSuccessful)
        assertEquals("hello", resp.body()!!.text)
    }
}


