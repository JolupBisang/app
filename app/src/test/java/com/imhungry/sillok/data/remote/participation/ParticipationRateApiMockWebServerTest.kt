package com.imhungry.sillok.data.remote.participation

import com.google.gson.Gson
import com.imhungry.sillok.data.model.participation.ParticipationRateHistoryResDto
import com.imhungry.sillok.data.model.participation.UserParticipationRateDto
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
class ParticipationRateApiMockWebServerTest {

    private lateinit var server: MockWebServer
    private lateinit var api: ParticipationRateApi
    private val gson = Gson()

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ParticipationRateApi::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun getParticipationRateHistory_contract() = runTest {
        val body = gson.toJson(
            ParticipationRateHistoryResDto(
                userParticipationRates = listOf(
                    UserParticipationRateDto(1, "n1", 0.5),
                    UserParticipationRateDto(2, "n2", 0.7)
                )
            )
        )
        server.enqueue(MockResponse().setResponseCode(200).setBody(body))

        val resp = api.getParticipationRateHistory(10)

        val req = server.takeRequest()
        assertEquals("/api/v1/meeting/10/participation-rate", req.path)
        assertEquals("GET", req.method)
        assertTrue(resp.isSuccessful)
        assertEquals(2, resp.body()!!.userParticipationRates.size)
    }
}


