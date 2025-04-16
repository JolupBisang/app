package com.imhungry.jjongseol.data.model.error

data class ApiError(
    val message: String?,
    val errorId: String?,
    val errors: Any? = null
)
