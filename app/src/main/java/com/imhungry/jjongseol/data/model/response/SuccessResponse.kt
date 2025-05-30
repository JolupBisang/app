package com.imhungry.jjongseol.data.model.response

data class SuccessResponse<T>(
    val message: String,
    val data: T
)
