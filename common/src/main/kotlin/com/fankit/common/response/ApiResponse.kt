package com.fankit.common.response

data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ErrorResponse? = null,
) {
    companion object {
        fun <T> success(data: T): ApiResponse<T> = ApiResponse(success = true, data = data)
        fun <T> success(): ApiResponse<T> = ApiResponse(success = true)
        fun <T> failure(code: String, message: String): ApiResponse<T> =
            ApiResponse(success = false, error = ErrorResponse(code, message))
    }
}

data class ErrorResponse(
    val code: String,
    val message: String,
)
