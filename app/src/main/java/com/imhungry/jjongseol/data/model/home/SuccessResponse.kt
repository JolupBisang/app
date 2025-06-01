package com.imhungry.jjongseol.data.model.home

data class SuccessResponse<T>(
    val status: Int,
    val message: String,
    val data: T
)