package com.fankit.user.application

import com.fankit.user.domain.exception.InvalidCredentialsException
import com.fankit.user.domain.model.AuthTokens
import com.fankit.user.domain.model.Email
import com.fankit.user.domain.port.inbound.LoginCommand
import com.fankit.user.domain.port.inbound.LoginUseCase
import com.fankit.user.domain.port.outbound.PasswordEncoder
import com.fankit.user.domain.port.outbound.RefreshTokenStore
import com.fankit.user.domain.port.outbound.TokenProvider
import com.fankit.user.domain.port.outbound.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LoginService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val tokenProvider: TokenProvider,
    private val refreshTokenStore: RefreshTokenStore,
    @Value("\${jwt.access-token-expiration}") private val accessTtlMs: Long,
    @Value("\${jwt.refresh-token-expiration}") private val refreshTtlMs: Long,
) : LoginUseCase {

    @Transactional(readOnly = true)
    override fun login(command: LoginCommand): AuthTokens {
        val user = userRepository.findByEmail(Email(command.email))
            ?: throw InvalidCredentialsException()

        // 사용자 미존재와 비밀번호 불일치를 동일 예외로 처리 — 계정 존재 여부 노출 방지
        if (!passwordEncoder.matches(command.password, user.password)) {
            throw InvalidCredentialsException()
        }

        val userId = user.id ?: error("저장된 User의 ID가 null — Repository 어댑터 버그")
        val accessToken = tokenProvider.issueAccessToken(userId, user.role)
        val refreshToken = tokenProvider.issueRefreshToken(userId)

        // Refresh Token을 Redis에 보관 → 탈취 시 즉시 무효화 가능 (서버 통제)
        refreshTokenStore.save(userId, refreshToken, refreshTtlMs / 1000)

        return AuthTokens(accessToken, refreshToken, accessTtlMs / 1000)
    }
}
