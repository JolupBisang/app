package com.imhungry.sillok.data.repository.places

import com.imhungry.sillok.BuildConfig
import com.imhungry.sillok.data.mapper.places.PlacesMapper
import com.imhungry.sillok.data.remote.places.PlacesApi
import com.imhungry.sillok.domain.model.places.PlaceSuggestion
import com.imhungry.sillok.domain.repository.places.PlacesRepository
import javax.inject.Inject

class PlacesRepositoryImpl @Inject constructor(
    private val placesApi: PlacesApi,
    private val mapper: PlacesMapper
) : PlacesRepository {

    override suspend fun searchPlaces(query: String): List<PlaceSuggestion> {
        return try {
            val response = placesApi.searchPlaces(
                clientId = BuildConfig.NAVER_CLIENT_ID,
                clientSecret = BuildConfig.NAVER_CLIENT_SECRET,
                query = query
            )

            val suggestions = response.items.map { mapper.mapToPlaceSuggestion(it) }
            suggestions
            
        } catch (e: Exception) {
            emptyList()
        }
    }
}