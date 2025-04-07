package com.d104.data.utils

import com.d104.data.remote.dto.ErrorResponseDto
import com.google.gson.Gson
import retrofit2.HttpException

object ErrorUtils {
    fun parseHttpError(exception: HttpException): ErrorResponseDto? {
        return try {
            val errorBody = exception.response()?.errorBody()?.string()
            Gson().fromJson(errorBody, ErrorResponseDto::class.java)
        } catch (e: Exception) {
            null
        }
    }
}