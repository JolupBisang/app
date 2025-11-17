package com.imhungry.sillok.data.model.meeting

data class MeetingListResDto(
    val content: List<MeetingDetailSummaryResDto>,
    val pageable: PageableDto,
    val size: Int,
    val number: Int,
    val numberOfElements: Int,
    val first: Boolean,
    val last: Boolean,
    val empty: Boolean
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

