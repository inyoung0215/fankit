package com.fankit.order.api

import com.fankit.common.response.ApiResponse
import com.fankit.order.domain.exception.IllegalOrderStateException
import com.fankit.order.domain.exception.OrderNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotFoundException::class)
    fun handleNotFound(e: OrderNotFoundException) = ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(ApiResponse.failure<Nothing>("ORDER_NOT_FOUND", e.message ?: ""))

    @ExceptionHandler(IllegalOrderStateException::class)
    fun handleIllegalState(e: IllegalOrderStateException) = ResponseEntity
        .status(HttpStatus.CONFLICT)
        .body(ApiResponse.failure<Nothing>("ORDER_ILLEGAL_STATE", e.message ?: ""))

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
