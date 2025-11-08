package com.imhungry.sillok.data.repository.participation

import com.imhungry.sillok.data.mapper.participation.ParticipationMapper
import com.imhungry.sillok.data.model.participation.ParticipationRateHistoryResDto
import com.imhungry.sillok.data.model.participation.UserParticipationRateDto
import com.imhungry.sillok.data.remote.participation.ParticipationRateApi
import com.imhungry.sillok.data.util.ApiResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class ParticipationRateRepositoryImplTest {

    private val api: ParticipationRateApi = mockk()
    private val mapper = ParticipationMapper()
    private val repository = ParticipationRateRepositoryImpl(api, mapper)

    @Test
    fun getParticipationRateHistory_success_mapsList() = runTest {
        val dto = ParticipationRateHistoryResDto(
            userParticipationRates = listOf(
                UserParticipationRateDto(userId = 1L, rate = 0.5, totalParticipationChunk = 10L),
                UserParticipationRateDto(userId = 2L, rate = 0.7, totalParticipationChunk = 15L)
            )
        )
        coEvery { api.getParticipationRateHistory(10) } returns Response.success(dto)

        val result = repository.getParticipationRateHistory(10)

        assertTrue(result is ApiResult.Success)
        val data = (result as ApiResult.Success).data
        assertEquals(2, data.size)
        assertEquals(2L, data[1].userId)
        assertEquals(0.7, data[1].rate)
        assertEquals(15L, data[1].totalParticipationChunk)
    }
}


