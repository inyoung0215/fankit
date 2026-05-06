package com.fankit.user.api

import com.fankit.common.response.ApiResponse
import com.fankit.user.api.dto.LoginRequest
import com.fankit.user.api.dto.RefreshRequest
import com.fankit.user.api.dto.SignupRequest
import com.fankit.user.api.dto.SignupResponse
import com.fankit.user.api.dto.TokenResponse
import com.fankit.user.domain.port.inbound.LoginUseCase
import com.fankit.user.domain.port.inbound.RefreshTokenUseCase
import com.fankit.user.domain.port.inbound.SignupUseCase
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val signupUseCase: SignupUseCase,
    private val loginUseCase: LoginUseCase,
    private val refreshTokenUseCase: RefreshTokenUseCase,
) {
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    fun signup(@RequestBody @Valid request: SignupRequest): ApiResponse<SignupResponse> {
        val userId = signupUseCase.signup(request.toCommand())
        return ApiResponse.success(SignupResponse(userId))
    }

    @PostMapping("/login")
    fun login(@RequestBody @Valid request: LoginRequest): ApiResponse<TokenResponse> =
        ApiResponse.success(TokenResponse.from(loginUseCase.login(request.toCommand())))

    @PostMapping("/refresh")
    fun refresh(@RequestBody @Valid request: RefreshRequest): ApiResponse<TokenResponse> =
        ApiResponse.success(TokenResponse.from(refreshTokenUseCase.refresh(request.refreshToken)))
}
