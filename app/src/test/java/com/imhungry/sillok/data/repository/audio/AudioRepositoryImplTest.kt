package com.imhungry.sillok.data.repository.audio

import com.imhungry.sillok.data.mapper.audio.AudioMapper
import com.imhungry.sillok.data.model.audio.AudioInfoDto
import com.imhungry.sillok.data.model.audio.AudioListResponseDto
import com.imhungry.sillok.data.remote.audio.AudioApi
import com.imhungry.sillok.data.util.ApiResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class AudioRepositoryImplTest {

    private val api: AudioApi = mockk()
    private val mapper = AudioMapper()
    private val repository = AudioRepositoryImpl(api, mapper)

    @Test
    fun getAudioList_success_mapsList() = runTest {
        val res = AudioListResponseDto(
            audioList = listOf(
                AudioInfoDto(userId = 1L, presignedUrl = "u1"),
                AudioInfoDto(userId = 2L, presignedUrl = "u2")
            )
        )
        coEvery { api.getAudioList(10) } returns Response.success(res)

        val result = repository.getAudioList(10)

        assertTrue(result is ApiResult.Success)
        val data = (result as ApiResult.Success).data
        assertEquals(2, data.size)
        assertEquals(1L, data[0].userId)
        assertEquals("u2", data[1].presignedUrl)
    }

    @Test
    fun embeddingAudio_success_returnsSuccess() = runTest {
        val tmp = File.createTempFile("test-audio", ".mp4")
        coEvery { api.embeddingAudio(any()) } returns Response.success(Unit)

        val result = repository.embeddingAudio(tmp)

        assertTrue(result is ApiResult.Success)
    }
}


