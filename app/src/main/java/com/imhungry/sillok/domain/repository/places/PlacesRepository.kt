package com.imhungry.sillok.domain.repository.places

import com.imhungry.sillok.domain.model.places.PlaceSuggestion

interface PlacesRepository {
    suspend fun searchPlaces(query: String): List<PlaceSuggestion>
}