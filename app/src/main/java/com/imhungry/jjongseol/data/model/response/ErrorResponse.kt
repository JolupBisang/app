package com.imhungry.jjongseol.data.model.response

data class ErrorResponse(
    val message: String,
    val errorId: String,
    val errors: Map<String, String>? = null
)
