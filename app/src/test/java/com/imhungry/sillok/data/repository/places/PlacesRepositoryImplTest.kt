package com.imhungry.sillok.data.repository.places

import com.imhungry.sillok.data.mapper.places.PlacesMapper
import com.imhungry.sillok.data.model.places.NaverPlaceDto
import com.imhungry.sillok.data.model.places.NaverPlacesResponseDto
import com.imhungry.sillok.data.remote.places.PlacesApi
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlacesRepositoryImplTest {

    private val api: PlacesApi = mockk()
    private val mapper = PlacesMapper()
    private val repository = PlacesRepositoryImpl(api, mapper)

    @Test
    fun searchPlaces_success_mapsSuggestions() = runTest {
        val dto = NaverPlacesResponseDto(
            lastBuildDate = "d", total = 1, start = 1, display = 1,
            items = listOf(
                NaverPlaceDto(
                    title = "<b>식당</b>", link = "l", category = "c",
                    description = "desc", telephone = "t",
                    address = "addr", roadAddress = "road",
                    mapx = "1", mapy = "2"
                )
            )
        )
        coEvery { api.searchPlaces(any(), any(), any(), any(), any(), any()) } returns dto

        val result = repository.searchPlaces("식당")

        assertEquals(1, result.size)
        assertEquals("식당", result.first().name)
        assertEquals("1_2", result.first().placeId)
    }
}


