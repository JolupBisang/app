package com.imhungry.sillok.domain.model.summary

data class Summary(
    val id: Long,
    val content: String,
    val isRecap: Boolean,
    val timestamp: String
)
