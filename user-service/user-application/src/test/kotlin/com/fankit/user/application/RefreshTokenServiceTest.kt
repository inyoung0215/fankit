package com.fankit.user.application

import com.fankit.user.domain.exception.InvalidRefreshTokenException
import com.fankit.user.domain.model.Email
import com.fankit.user.domain.model.EncodedPassword
import com.fankit.user.domain.model.Role
import com.fankit.user.domain.model.User
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

class RefreshTokenServiceTest {

    private val userRepository: UserRepository = mockk()
    private val tokenProvider: TokenProvider = mockk()
    private val refreshTokenStore: RefreshTokenStore = mockk()

    private val service = RefreshTokenService(
        userRepository, tokenProvider, refreshTokenStore,
        accessTtlMs = 1_800_000L,
        refreshTtlMs = 604_800_000L,
    )

    private val storedUser = User(
        id = 1L,
        email = Email("a@b.com"),
        password = EncodedPassword("enc"),
        nickname = "nick",
        role = Role.USER,
    )

    @Test
    fun `토큰 서명 검증 실패 시 InvalidRefreshTokenException`() {
        every { tokenProvider.parseUserId("bad-token") } throws RuntimeException("signature mismatch")

        shouldThrow<InvalidRefreshTokenException> {
            service.refresh("bad-token")
        }
    }

    @Test
    fun `Redis에 보관된 토큰이 없으면 InvalidRefreshTokenException`() {
        every { tokenProvider.parseUserId("token") } returns 1L
        every { refreshTokenStore.findByUserId(1L) } returns null

        shouldThrow<InvalidRefreshTokenException> {
            service.refresh("token")
        }
    }

    @Test
    fun `Redis 저장값과 다른 토큰이면 InvalidRefreshTokenException — 도용 방어`() {
        every { tokenProvider.parseUserId("token") } returns 1L
        every { refreshTokenStore.findByUserId(1L) } returns "different-token"

        shouldThrow<InvalidRefreshTokenException> {
            service.refresh("token")
        }
    }

    @Test
    fun `정상 갱신 시 Refresh Token Rotation — 새 access·refresh 발급 + Redis 갱신`() {
        every { tokenProvider.parseUserId("old-refresh") } returns 1L
        every { refreshTokenStore.findByUserId(1L) } returns "old-refresh"
        every { userRepository.findById(1L) } returns storedUser
        every { tokenProvider.issueAccessToken(1L, Role.USER) } returns "new-access"
        every { tokenProvider.issueRefreshToken(1L) } returns "new-refresh"
        every { refreshTokenStore.save(1L, "new-refresh", any()) } just Runs

        val tokens = service.refresh("old-refresh")

        tokens.accessToken shouldBe "new-access"
        tokens.refreshToken shouldBe "new-refresh"
        verify { refreshTokenStore.save(1L, "new-refresh", any()) }
    }
}
