package com.fankit.user.application

import com.fankit.user.domain.exception.EmailAlreadyExistsException
import com.fankit.user.domain.model.Email
import com.fankit.user.domain.model.User
import com.fankit.user.domain.port.inbound.SignupCommand
import com.fankit.user.domain.port.inbound.SignupUseCase
import com.fankit.user.domain.port.outbound.PasswordEncoder
import com.fankit.user.domain.port.outbound.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SignupService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) : SignupUseCase {

    @Transactional
    override fun signup(command: SignupCommand): Long {
        val email = Email(command.email)

        // 중복 체크 — DB unique 제약과 이중 방어
        // (race condition 시 DB 제약이 최후 보루, 여기선 친절한 메시지 제공)
        if (userRepository.existsByEmail(email)) {
            throw EmailAlreadyExistsException(command.email)
        }

        val encoded = passwordEncoder.encode(command.password)
        val user = User.create(email, encoded, command.nickname)
        return userRepository.save(user).id
            ?: error("저장된 User에 ID가 없다 — Repository 어댑터 버그")
    }
}
