package com.imhungry.sillok.data.model.segment

data class SegmentListResDto(
    val content: List<SegmentItemDto>,
    val pageable: PageableDto,
    val size: Int,
    val number: Int,
    val numberOfElements: Int,
    val first: Boolean,
    val last: Boolean,
    val empty: Boolean,
    val totalElements: Long? = null  // 전체 요소 개수 (서버에서 제공하는 경우)
)

data class SegmentItemDto(
    val id: Long,
    val userId: Long,
    val segmentOrder: Int,
    val timestamp: String,
    val text: String,
    val lang: String
)

data class PageableDto(
    val pageNumber: Int,
    val pageSize: Int,
    val sort: SortDto,
    val offset: Int,
    val unpaged: Boolean,
    val paged: Boolean
)

data class SortDto(
    val empty: Boolean,
    val unsorted: Boolean,
    val sorted: Boolean
)
