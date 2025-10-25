package com.imhungry.sillok.domain.usecase.places

import com.imhungry.sillok.domain.model.places.PlaceSuggestion
import com.imhungry.sillok.domain.repository.places.PlacesRepository
import javax.inject.Inject

class SearchPlacesUseCase @Inject constructor(
    private val placesRepository: PlacesRepository
) {
    suspend operator fun invoke(query: String): List<PlaceSuggestion> {
        return placesRepository.searchPlaces(query)
    }
}