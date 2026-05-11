package com.fankit.admin.api

import com.fankit.admin.domain.exception.ExternalServiceCallFailedException
import com.fankit.common.response.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ExternalServiceCallFailedException::class)
    fun handleExternalCallFail(e: ExternalServiceCallFailedException) = ResponseEntity
        .status(HttpStatus.BAD_GATEWAY)
        .body(ApiResponse.failure<Nothing>("EXTERNAL_CALL_FAILED", e.message ?: ""))

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(e: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Nothing>> {
        val msg = e.bindingResult.fieldErrors.joinToString(", ") { "${it.field}: ${it.defaultMessage}" }
        return ResponseEntity.badRequest().body(ApiResponse.failure("VALIDATION_FAILED", msg))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArg(e: IllegalArgumentException) = ResponseEntity
        .badRequest()
        .body(ApiResponse.failure<Nothing>("INVALID_ARGUMENT", e.message ?: "잘못된 요청"))
}
