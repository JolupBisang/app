package com.imhungry.jjongseol.data.network.client

import com.google.gson.Gson
import com.imhungry.jjongseol.data.model.error.ApiError
import retrofit2.HttpException

fun handleHttpException(e: HttpException): ApiError {
    val errorBody = e.response()?.errorBody()?.string()
    return try {
        val parsed = Gson().fromJson(errorBody, ApiError::class.java)
        parsed.copy(message = parsed.message ?: "오류가 발생했습니다 (${e.code()})")
    } catch (ex: Exception) {
        ApiError(message = "오류가 발생했습니다 (${e.code()})", errorId = null)
    }
}

