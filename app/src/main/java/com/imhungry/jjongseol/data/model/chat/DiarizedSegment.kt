package com.imhungry.jjongseol.data.model.chat

data class DiarizedSegment(
    val timestamp: String,
    val userId: Int,
    val order: Int,
    val text: String
)


