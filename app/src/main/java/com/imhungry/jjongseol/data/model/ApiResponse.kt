package com.imhungry.jjongseol.data.model

data class ApiResponse<T>(
    val message: String,
    val data: T
)
