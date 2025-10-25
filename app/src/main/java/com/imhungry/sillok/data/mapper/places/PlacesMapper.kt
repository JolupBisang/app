package com.imhungry.sillok.data.mapper.places

import com.imhungry.sillok.data.model.places.NaverPlaceDto
import com.imhungry.sillok.domain.model.places.PlaceSuggestion
import javax.inject.Inject

class PlacesMapper @Inject constructor() {
    fun mapToPlaceSuggestion(naverPlace: NaverPlaceDto): PlaceSuggestion {
        return PlaceSuggestion(
            placeId = naverPlace.mapx + "_" + naverPlace.mapy, // 좌표를 ID로 사용
            name = naverPlace.title.replace(Regex("<[^>]*>"), ""), // HTML 태그 제거
            address = naverPlace.roadAddress.ifEmpty { naverPlace.address }
        )
    }
}