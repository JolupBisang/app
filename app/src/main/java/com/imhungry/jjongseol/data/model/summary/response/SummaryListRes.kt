package com.imhungry.jjongseol.data.model.summary.response

data class SummaryListRes(
    val id: Long,
    val content: String,
    val isRecap: Boolean,
    val timestamp: String
)