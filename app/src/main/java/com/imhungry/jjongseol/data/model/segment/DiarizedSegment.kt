package com.imhungry.jjongseol.data.model.segment

data class DiarizedSegment(
    val timestamp: String,
    val userId: Long,
    val order: Int,
    val text: String
)


