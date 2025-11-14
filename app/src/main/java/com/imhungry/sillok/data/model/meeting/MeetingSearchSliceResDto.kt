package com.imhungry.sillok.data.model.meeting

/**
 * Spring Slice 응답 DTO
 * 무한 스크롤을 위한 페이지네이션 응답
 */
data class MeetingSearchSliceResDto(
    val content: List<MeetingDetailSummaryResDto>,
    val number: Int,
    val size: Int,
    val numberOfElements: Int,
    val first: Boolean,
    val last: Boolean,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)

