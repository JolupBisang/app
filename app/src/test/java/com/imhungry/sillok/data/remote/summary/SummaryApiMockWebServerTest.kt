package com.imhungry.sillok.data.remote.summary

import com.google.gson.Gson
import com.imhungry.sillok.data.model.summary.SummaryListResDto
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
class SummaryApiMockWebServerTest {

    private lateinit var server: MockWebServer
    private lateinit var api: SummaryApi
    private val gson = Gson()

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SummaryApi::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun getSummaries_contract() = runTest {
        val body = gson.toJson(SummaryListResDto(id = 1, content = "c", isRecap = false, timestamp = "t"))
        server.enqueue(MockResponse().setResponseCode(200).setBody(body))

        val resp = api.getSummaries(10, false)

        val req = server.takeRequest()
        assertEquals("/api/v1/meetings/10/summary?isRecap=false", req.path)
        assertEquals("GET", req.method)
        assertTrue(resp.isSuccessful)
        assertEquals("c", resp.body()!!.content)
    }
}


