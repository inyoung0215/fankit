package com.fankit.user.domain.port.inbound

import com.fankit.user.domain.model.AuthTokens

interface LoginUseCase {
    fun login(command: LoginCommand): AuthTokens
}

data class LoginCommand(
    val email: String,
    val password: String,
)
