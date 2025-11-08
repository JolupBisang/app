package com.imhungry.sillok.data.model.summary

data class SummaryListResDto(
    val id: Long,
    val content: List<String>,
    val isRecap: Boolean,
    val generatedDateTime: String
)
