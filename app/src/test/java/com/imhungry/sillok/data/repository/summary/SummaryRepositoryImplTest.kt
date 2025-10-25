package com.imhungry.sillok.data.repository.summary

import com.imhungry.sillok.data.mapper.summary.SummaryMapper
import com.imhungry.sillok.data.model.summary.SummaryListResDto
import com.imhungry.sillok.data.remote.summary.SummaryApi
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
class SummaryRepositoryImplTest {

    private val api: SummaryApi = mockk()
    private val mapper = SummaryMapper()
    private val repository = SummaryRepositoryImpl(api, mapper)

    @Test
    fun getSummaries_success_mapsList() = runTest {
        val dto = SummaryListResDto(id = 7L, content = "c", isRecap = false, timestamp = "t")
        coEvery { api.getSummaries(10, false) } returns Response.success(dto)

        val result = repository.getSummaries(10, false, 0, 30)

        assertTrue(result is ApiResult.Success)
        val data = (result as ApiResult.Success).data
        assertEquals(1, data.size)
        assertEquals("c", data.first().content)
    }
}


