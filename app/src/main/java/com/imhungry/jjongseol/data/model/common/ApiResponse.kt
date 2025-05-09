package com.imhungry.jjongseol.data.model.common

data class ApiResponse<T>(
    val message: String,
    val data: T
)
