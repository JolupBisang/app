package com.imhungry.jjongseol.data.network.client

import com.google.gson.Gson
import com.imhungry.jjongseol.data.model.error.ApiError
import retrofit2.HttpException

fun handleHttpException(e: HttpException): ApiError {
    return try {
        val errorJson = e.response()?.errorBody()?.string()
        Gson().fromJson(errorJson, ApiError::class.java)
    } catch (ex: Exception) {
        ApiError(message = "서버 오류가 발생했습니다.", errorId = null)
    }
}


