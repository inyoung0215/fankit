package com.fankit.goods.api

import com.fankit.common.response.ApiResponse
import com.fankit.goods.domain.exception.ForbiddenGoodsAccessException
import com.fankit.goods.domain.exception.GoodsNotFoundException
import com.fankit.goods.domain.exception.IllegalGoodsStateTransitionException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(GoodsNotFoundException::class)
    fun handleNotFound(e: GoodsNotFoundException): ResponseEntity<ApiResponse<Nothing>> =
        ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.failure("GOODS_NOT_FOUND", e.message ?: ""))

    @ExceptionHandler(IllegalGoodsStateTransitionException::class)
    fun handleStateTransition(e: IllegalGoodsStateTransitionException): ResponseEntity<ApiResponse<Nothing>> =
        ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiResponse.failure("GOODS_ILLEGAL_STATE", e.message ?: ""))

    @ExceptionHandler(ForbiddenGoodsAccessException::class)
    fun handleForbidden(e: ForbiddenGoodsAccessException): ResponseEntity<ApiResponse<Nothing>> =
        ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ApiResponse.failure("GOODS_FORBIDDEN", e.message ?: ""))

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(e: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Nothing>> {
        val msg = e.bindingResult.fieldErrors.joinToString(", ") { "${it.field}: ${it.defaultMessage}" }
        return ResponseEntity.badRequest().body(ApiResponse.failure("VALIDATION_FAILED", msg))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArg(e: IllegalArgumentException): ResponseEntity<ApiResponse<Nothing>> =
        ResponseEntity.badRequest().body(ApiResponse.failure("INVALID_ARGUMENT", e.message ?: "잘못된 요청"))
}
