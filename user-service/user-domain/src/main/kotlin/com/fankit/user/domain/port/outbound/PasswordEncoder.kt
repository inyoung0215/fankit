package com.fankit.user.domain.port.outbound

import com.fankit.user.domain.model.EncodedPassword

// 도메인이 BCrypt/Argon2 등 구체 알고리즘을 모르도록 추상화한 port
// 인프라가 Spring Security의 BCryptPasswordEncoder로 구현
interface PasswordEncoder {
    fun encode(rawPassword: String): EncodedPassword
    fun matches(rawPassword: String, encoded: EncodedPassword): Boolean
}
