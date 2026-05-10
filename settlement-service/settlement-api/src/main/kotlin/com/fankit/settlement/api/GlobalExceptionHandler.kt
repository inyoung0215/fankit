package com.fankit.settlement.api

import com.fankit.common.response.ApiResponse
import com.fankit.settlement.domain.exception.SettlementJobAlreadyRunException
import com.fankit.settlement.domain.exception.SettlementNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(SettlementNotFoundException::class)
    fun handleNotFound(e: SettlementNotFoundException) = ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(ApiResponse.failure<Nothing>("SETTLEMENT_NOT_FOUND", e.message ?: ""))

    @ExceptionHandler(SettlementJobAlreadyRunException::class)
    fun handleAlready(e: SettlementJobAlreadyRunException) = ResponseEntity
        .status(HttpStatus.CONFLICT)
        .body(ApiResponse.failure<Nothing>("SETTLEMENT_ALREADY_RUN", e.message ?: ""))

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
