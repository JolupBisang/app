package com.imhungry.sillok.data.model.feedback

data class FeedbackListResDto(
    val content: List<FeedbackItemDto>,
    val pageable: PageableDto,
    val size: Int,
    val number: Int,
    val numberOfElements: Int,
    val first: Boolean,
    val last: Boolean,
    val empty: Boolean
)

data class FeedbackItemDto(
    val id: Long,
    val comment: String,
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
