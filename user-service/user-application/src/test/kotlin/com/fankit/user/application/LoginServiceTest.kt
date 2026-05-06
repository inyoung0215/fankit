package com.fankit.user.application

import com.fankit.user.domain.exception.InvalidCredentialsException
import com.fankit.user.domain.model.Email
import com.fankit.user.domain.model.EncodedPassword
import com.fankit.user.domain.model.Role
import com.fankit.user.domain.model.User
import com.fankit.user.domain.port.inbound.LoginCommand
import com.fankit.user.domain.port.outbound.PasswordEncoder
import com.fankit.user.domain.port.outbound.RefreshTokenStore
import com.fankit.user.domain.port.outbound.TokenProvider
import com.fankit.user.domain.port.outbound.UserRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class LoginServiceTest {

    private val userRepository: UserRepository = mockk()
    private val passwordEncoder: PasswordEncoder = mockk()
    private val tokenProvider: TokenProvider = mockk()
    private val refreshTokenStore: RefreshTokenStore = mockk()

    private val accessTtlMs = 1_800_000L      // 30분
    private val refreshTtlMs = 604_800_000L   // 7일

    private val service = LoginService(
        userRepository, passwordEncoder, tokenProvider, refreshTokenStore,
        accessTtlMs = accessTtlMs, refreshTtlMs = refreshTtlMs,
    )

    private val storedUser = User(
        id = 1L,
        email = Email("a@b.com"),
        password = EncodedPassword("enc"),
        nickname = "nick",
        role = Role.USER,
    )

    @Test
    fun `존재하지 않는 이메일이면 InvalidCredentialsException`() {
        // mockk는 value class any() 매처에서 임의 hash로 인스턴스를 만들어
        // Email.init {} 검증에 걸리므로, value class는 명시적 인스턴스로 stub한다
        every { userRepository.findByEmail(Email("none@x.com")) } returns null

        shouldThrow<InvalidCredentialsException> {
            service.login(LoginCommand("none@x.com", "p"))
        }
    }

    @Test
    fun `비밀번호 불일치 시 InvalidCredentialsException — 계정 존재 여부는 노출되지 않는다`() {
        every { userRepository.findByEmail(Email("a@b.com")) } returns storedUser
        every { passwordEncoder.matches("wrong", storedUser.password) } returns false

        shouldThrow<InvalidCredentialsException> {
            service.login(LoginCommand("a@b.com", "wrong"))
        }
    }

    @Test
    fun `정상 로그인 시 access·refresh 발급 + Refresh를 Redis에 TTL과 함께 저장`() {
        every { userRepository.findByEmail(Email("a@b.com")) } returns storedUser
        every { passwordEncoder.matches("p", storedUser.password) } returns true
        every { tokenProvider.issueAccessToken(1L, Role.USER) } returns "access-token"
        every { tokenProvider.issueRefreshToken(1L) } returns "refresh-token"
        every { refreshTokenStore.save(1L, "refresh-token", any()) } just Runs

        val tokens = service.login(LoginCommand("a@b.com", "p"))

        tokens.accessToken shouldBe "access-token"
        tokens.refreshToken shouldBe "refresh-token"
        tokens.accessTokenExpiresInSeconds shouldBe accessTtlMs / 1000
        verify { refreshTokenStore.save(1L, "refresh-token", refreshTtlMs / 1000) }
    }
}
