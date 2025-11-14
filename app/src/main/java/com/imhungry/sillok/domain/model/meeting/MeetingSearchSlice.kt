package com.imhungry.sillok.domain.model.meeting

/**
 * 무한 스크롤을 위한 검색 결과 Slice
 */
data class MeetingSearchSlice(
    val content: List<MeetingDetailSummary>,
    val number: Int,
    val size: Int,
    val numberOfElements: Int,
    val first: Boolean,
    val last: Boolean,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)

