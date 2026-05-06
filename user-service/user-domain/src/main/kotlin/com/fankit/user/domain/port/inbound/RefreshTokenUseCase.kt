package com.fankit.user.domain.port.inbound

import com.fankit.user.domain.model.AuthTokens

interface RefreshTokenUseCase {
    fun refresh(refreshToken: String): AuthTokens
}
