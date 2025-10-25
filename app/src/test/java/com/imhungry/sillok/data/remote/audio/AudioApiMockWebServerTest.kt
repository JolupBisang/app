package com.imhungry.sillok.data.remote.audio

import com.google.gson.Gson
import com.imhungry.sillok.data.model.audio.AudioInfoDto
import com.imhungry.sillok.data.model.audio.AudioListResponseDto
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
class AudioApiMockWebServerTest {

    private lateinit var server: MockWebServer
    private lateinit var api: AudioApi
    private val gson = Gson()

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AudioApi::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun getAudioList_contract() = runTest {
        val body = gson.toJson(
            AudioListResponseDto(audioList = listOf(
                AudioInfoDto(1, "u1"), AudioInfoDto(2, "u2")
            ))
        )
        server.enqueue(MockResponse().setResponseCode(200).setBody(body))

        val resp = api.getAudioList(10)

        val req = server.takeRequest()
        assertEquals("/api/v1/meeting/10/fullAudio", req.path)
        assertEquals("GET", req.method)
        assertTrue(resp.isSuccessful)
        assertEquals(2, resp.body()!!.audioList.size)
    }
}


