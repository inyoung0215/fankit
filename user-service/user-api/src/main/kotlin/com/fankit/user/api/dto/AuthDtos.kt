package com.fankit.user.api.dto

import com.fankit.user.domain.model.AuthTokens
import com.fankit.user.domain.port.inbound.LoginCommand
import com.fankit.user.domain.port.inbound.SignupCommand
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class SignupRequest(
    @field:Email(message = "올바른 이메일 형식이 아닙니다")
    @field:NotBlank
    val email: String,

    @field:Size(min = 8, max = 30, message = "비밀번호는 8~30자여야 합니다")
    val password: String,

    @field:NotBlank
    @field:Size(max = 50)
    val nickname: String,
) {
    fun toCommand() = SignupCommand(email, password, nickname)
}

data class SignupResponse(val userId: Long)

data class LoginRequest(
    @field:Email
    @field:NotBlank
    val email: String,

    @field:NotBlank
    val password: String,
) {
    fun toCommand() = LoginCommand(email, password)
}

data class RefreshRequest(
    @field:NotBlank
    val refreshToken: String,
)

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
) {
    companion object {
        fun from(t: AuthTokens) = TokenResponse(t.accessToken, t.refreshToken, t.accessTokenExpiresInSeconds)
    }
}
