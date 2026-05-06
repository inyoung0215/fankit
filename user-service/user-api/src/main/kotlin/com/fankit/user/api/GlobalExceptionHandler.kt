package com.fankit.user.api

import com.fankit.common.response.ApiResponse
import com.fankit.user.domain.exception.EmailAlreadyExistsException
import com.fankit.user.domain.exception.InvalidCredentialsException
import com.fankit.user.domain.exception.InvalidRefreshTokenException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException::class)
    fun handleEmailDuplicated(e: EmailAlreadyExistsException): ResponseEntity<ApiResponse<Nothing>> =
        ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiResponse.failure("USER_EMAIL_DUPLICATED", e.message ?: ""))

    @ExceptionHandler(InvalidCredentialsException::class)
    fun handleInvalidCredentials(e: InvalidCredentialsException): ResponseEntity<ApiResponse<Nothing>> =
        ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.failure("USER_INVALID_CREDENTIALS", e.message ?: ""))

    @ExceptionHandler(InvalidRefreshTokenException::class)
    fun handleInvalidRefresh(e: InvalidRefreshTokenException): ResponseEntity<ApiResponse<Nothing>> =
        ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.failure("USER_INVALID_REFRESH_TOKEN", e.message ?: ""))

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(e: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Nothing>> {
        val msg = e.bindingResult.fieldErrors.joinToString(", ") { "${it.field}: ${it.defaultMessage}" }
        return ResponseEntity.badRequest().body(ApiResponse.failure("VALIDATION_FAILED", msg))
    }

    // 도메인 invariant 위반 (Email 형식, 닉네임 길이 등 require {})
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArg(e: IllegalArgumentException): ResponseEntity<ApiResponse<Nothing>> =
        ResponseEntity.badRequest().body(ApiResponse.failure("INVALID_ARGUMENT", e.message ?: "잘못된 요청"))
}
