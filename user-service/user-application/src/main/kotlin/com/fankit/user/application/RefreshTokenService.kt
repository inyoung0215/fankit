package com.fankit.user.application

import com.fankit.user.domain.exception.InvalidRefreshTokenException
import com.fankit.user.domain.model.AuthTokens
import com.fankit.user.domain.port.inbound.RefreshTokenUseCase
import com.fankit.user.domain.port.outbound.RefreshTokenStore
import com.fankit.user.domain.port.outbound.TokenProvider
import com.fankit.user.domain.port.outbound.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class RefreshTokenService(
    private val userRepository: UserRepository,
    private val tokenProvider: TokenProvider,
    private val refreshTokenStore: RefreshTokenStore,
    @Value("\${jwt.access-token-expiration}") private val accessTtlMs: Long,
    @Value("\${jwt.refresh-token-expiration}") private val refreshTtlMs: Long,
) : RefreshTokenUseCase {

    override fun refresh(refreshToken: String): AuthTokens {
        // ① 토큰 자체 유효성: 서명/만료 검증
        val userId = try {
            tokenProvider.parseUserId(refreshToken)
        } catch (e: Exception) {
            throw InvalidRefreshTokenException()
        }

        // ② Redis 보관본과 일치 여부: 탈취·재사용 차단
        // (서명만 검증하면 stateless 토큰의 한계 — 탈취된 토큰을 거부할 방법 없음)
        val stored = refreshTokenStore.findByUserId(userId)
            ?: throw InvalidRefreshTokenException()
        if (stored != refreshToken) {
            // 다른 토큰이 보관 중 = 누군가가 새 로그인을 했거나 토큰 도용 시도
            throw InvalidRefreshTokenException()
        }

        val user = userRepository.findById(userId) ?: throw InvalidRefreshTokenException()

        // ③ Refresh Token Rotation — 매 갱신마다 새 Refresh도 발급, 기존 무효화
        //   탈취된 Refresh가 한 번 쓰이면 곧바로 무용지물이 되도록 만든 안전장치
        val newAccess = tokenProvider.issueAccessToken(userId, user.role)
        val newRefresh = tokenProvider.issueRefreshToken(userId)
        refreshTokenStore.save(userId, newRefresh, refreshTtlMs / 1000)

        return AuthTokens(newAccess, newRefresh, accessTtlMs / 1000)
    }
}
