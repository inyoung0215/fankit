package com.fankit.user.domain.port.inbound

interface SignupUseCase {
    fun signup(command: SignupCommand): Long
}

data class SignupCommand(
    val email: String,
    val password: String,
    val nickname: String,
)
