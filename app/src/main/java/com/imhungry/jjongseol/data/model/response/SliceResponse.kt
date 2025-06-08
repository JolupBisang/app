package com.imhungry.jjongseol.data.model.response

data class SliceResponse<T>(
    val content: List<T>,
    val pageable: PageableInfo?,
    val last: Boolean,
    val first: Boolean,
    val size: Int,
    val number: Int,
    val numberOfElements: Int,
    val empty: Boolean
)

data class PageableInfo(
    val pageNumber: Int,
    val pageSize: Int,
    val offset: Int,
    val paged: Boolean,
    val unpaged: Boolean,
    val sort: SortInfo
)

data class SortInfo(
    val empty: Boolean,
    val unsorted: Boolean,
    val sorted: Boolean
)
