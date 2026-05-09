package com.fankit.payment.api

import com.fankit.common.response.ApiResponse
import com.fankit.payment.domain.exception.IllegalPaymentStateException
import com.fankit.payment.domain.exception.OrderAlreadyPaidException
import com.fankit.payment.domain.exception.PaymentNotFoundException
import com.fankit.payment.domain.exception.PgUnavailableException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(PaymentNotFoundException::class)
    fun handleNotFound(e: PaymentNotFoundException) = ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(ApiResponse.failure<Nothing>("PAYMENT_NOT_FOUND", e.message ?: ""))

    @ExceptionHandler(OrderAlreadyPaidException::class)
    fun handleOrderPaid(e: OrderAlreadyPaidException) = ResponseEntity
        .status(HttpStatus.CONFLICT)
        .body(ApiResponse.failure<Nothing>("ORDER_ALREADY_PAID", e.message ?: ""))

    @ExceptionHandler(IllegalPaymentStateException::class)
    fun handleIllegalState(e: IllegalPaymentStateException) = ResponseEntity
        .status(HttpStatus.CONFLICT)
        .body(ApiResponse.failure<Nothing>("PAYMENT_ILLEGAL_STATE", e.message ?: ""))

    @ExceptionHandler(PgUnavailableException::class)
    fun handlePgDown(e: PgUnavailableException) = ResponseEntity
        // 503 Service Unavailable — Circuit Breaker OPEN 시 적절
        .status(HttpStatus.SERVICE_UNAVAILABLE)
        .body(ApiResponse.failure<Nothing>("PG_UNAVAILABLE", e.message ?: ""))

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
