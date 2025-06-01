package com.imhungry.jjongseol.data.model.response

data class SuccessResponse<T>(
    val message: String,
    val data: T
)

/*data class SuccessResponse<T>(
    val status: Int,
    val message: String,
    val data: T
)*/