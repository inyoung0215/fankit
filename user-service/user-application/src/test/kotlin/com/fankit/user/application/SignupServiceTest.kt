package com.fankit.user.application

import com.fankit.user.domain.exception.EmailAlreadyExistsException
import com.fankit.user.domain.model.EncodedPassword
import com.fankit.user.domain.model.Role
import com.fankit.user.domain.port.inbound.SignupCommand
import com.fankit.user.domain.port.outbound.PasswordEncoder
import com.fankit.user.domain.port.outbound.UserRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class SignupServiceTest {

    private val userRepository: UserRepository = mockk()
    private val passwordEncoder: PasswordEncoder = mockk()
    private val service = SignupService(userRepository, passwordEncoder)

    @Test
    fun `이메일이 이미 존재하면 EmailAlreadyExistsException 발생하고 비밀번호 인코딩은 호출되지 않는다`() {
        // mockk는 value class에 any() 매처를 쓸 때 내부적으로 임의 값으로 인스턴스를 만들어
        // hash를 계산함 → Email.init {} 검증에 걸려 IllegalArgumentException 발생
        // 따라서 value class는 명시적 인스턴스로 stub
        every { userRepository.existsByEmail(com.fankit.user.domain.model.Email("a@b.com")) } returns true

        shouldThrow<EmailAlreadyExistsException> {
            service.signup(SignupCommand("a@b.com", "password123", "nick"))
        }

        verify(exactly = 0) { passwordEncoder.encode(any()) }
        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `정상 가입 시 비밀번호를 인코딩하고 USER 권한으로 저장한다`() {
        every { userRepository.existsByEmail(com.fankit.user.domain.model.Email("a@b.com")) } returns false
        every { passwordEncoder.encode("password123") } returns EncodedPassword("encoded-hash")
        every { userRepository.save(any()) } answers {
            firstArg<com.fankit.user.domain.model.User>().copy(id = 42L)
        }

        val id = service.signup(SignupCommand("a@b.com", "password123", "nick"))

        id shouldBe 42L
        verify {
            userRepository.save(match {
                it.email.value == "a@b.com" &&
                    it.password.value == "encoded-hash" &&
                    it.nickname == "nick" &&
                    it.role == Role.USER
            })
        }
    }
}
