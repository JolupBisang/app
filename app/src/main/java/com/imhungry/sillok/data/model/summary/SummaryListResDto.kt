package com.imhungry.sillok.data.model.summary

data class SummaryListResDto(
    val content: List<SummaryItemDto>,
    val pageable: PageableDto,
    val size: Int,
    val number: Int,
    val numberOfElements: Int,
    val first: Boolean,
    val last: Boolean,
    val empty: Boolean
)

data class SummaryItemDto(
    val id: Long,
    val content: String,
    val isRecap: Boolean,
    val generatedDateTime: String
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
