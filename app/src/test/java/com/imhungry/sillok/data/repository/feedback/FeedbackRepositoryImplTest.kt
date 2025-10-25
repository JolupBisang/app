package com.imhungry.sillok.data.repository.feedback

import com.imhungry.sillok.data.mapper.feedback.FeedbackMapper
import com.imhungry.sillok.data.model.feedback.FeedbackListResDto
import com.imhungry.sillok.data.remote.feedback.FeedbackApi
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
class FeedbackRepositoryImplTest {

    private val api: FeedbackApi = mockk()
    private val mapper = FeedbackMapper()
    private val repository = FeedbackRepositoryImpl(api, mapper)

    @Test
    fun getFeedbacks_success_mapsDomainList() = runTest {
        val dto = FeedbackListResDto(id = 1L, comment = "good", timestamp = "t")
        coEvery { api.getFeedbacks(10) } returns Response.success(dto)

        val result = repository.getFeedbacks(10, 0, 30)

        assertTrue(result is ApiResult.Success)
        val data = (result as ApiResult.Success).data
        assertEquals(1, data.size)
        assertEquals("good", data.first().comment)
    }
}


