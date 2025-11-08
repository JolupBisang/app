package com.imhungry.sillok.data.repository.segment

import com.imhungry.sillok.data.mapper.segment.SegmentMapper
import com.imhungry.sillok.data.model.segment.SegmentListResDto
import com.imhungry.sillok.data.remote.segment.SegmentApi
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
class SegmentRepositoryImplTest {

    private val api: SegmentApi = mockk()
    private val mapper = SegmentMapper()
    private val repository = SegmentRepositoryImpl(api, mapper)

    @Test
    fun getSegments_success_mapsDomainList() = runTest {
        val dto = SegmentListResDto(
            id = 1L, userId = 2L,
            segmentOrder = 1, timestamp = "t", text = "hello", lang = "ko"
        )
        coEvery { api.getSegments(10) } returns Response.success(dto)

        val result = repository.getSegments(10, 0, 40)

        assertTrue(result is ApiResult.Success)
        val item = (result as ApiResult.Success).data.first()
        assertEquals("hello", item.text)
        assertEquals(2L, item.userId)
    }
}


