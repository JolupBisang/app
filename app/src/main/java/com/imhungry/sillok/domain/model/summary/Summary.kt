package com.imhungry.sillok.domain.model.summary

data class Summary(
    val id: Long,
    val content: List<String>,
    val isRecap: Boolean,
    val generatedDateTime: String
)
