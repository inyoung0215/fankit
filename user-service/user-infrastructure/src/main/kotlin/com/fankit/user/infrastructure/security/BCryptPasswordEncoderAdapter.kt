package com.fankit.user.infrastructure.security

import com.fankit.user.domain.model.EncodedPassword
import com.fankit.user.domain.port.outbound.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.security.crypto.password.PasswordEncoder as SpringPasswordEncoder

// 도메인의 PasswordEncoder port → Spring Security BCryptPasswordEncoder 어댑터
@Component
class BCryptPasswordEncoderAdapter(
    private val springEncoder: SpringPasswordEncoder,
) : PasswordEncoder {

    override fun encode(rawPassword: String): EncodedPassword =
        EncodedPassword(springEncoder.encode(rawPassword))

    override fun matches(rawPassword: String, encoded: EncodedPassword): Boolean =
        springEncoder.matches(rawPassword, encoded.value)
}
