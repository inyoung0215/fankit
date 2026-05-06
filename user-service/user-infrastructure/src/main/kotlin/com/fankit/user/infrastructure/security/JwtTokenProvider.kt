package com.fankit.user.infrastructure.security

import com.fankit.user.domain.model.Role
import com.fankit.user.domain.port.outbound.TokenProvider
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    private val properties: JwtProperties,
) : TokenProvider {

    // HS256 알고리즘은 256비트 이상 시크릿 키를 요구 (jjwt 0.12부터 강제)
    private val key: SecretKey = Keys.hmacShaKeyFor(properties.secret.toByteArray(Charsets.UTF_8))

    override fun issueAccessToken(userId: Long, role: Role): String =
        Jwts.builder()
            .subject(userId.toString())
            .claim("role", role.name)
            .claim("type", TYPE_ACCESS)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + properties.accessTokenExpiration))
            .signWith(key)
            .compact()

    override fun issueRefreshToken(userId: Long): String =
        Jwts.builder()
            .subject(userId.toString())
            .claim("type", TYPE_REFRESH)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + properties.refreshTokenExpiration))
            .signWith(key)
            .compact()

    override fun parseUserId(token: String): Long =
        parse(token).subject.toLong()

    override fun parseRole(token: String): Role =
        Role.valueOf(parse(token).get("role", String::class.java))

    private fun parse(token: String): Claims =
        Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload

    companion object {
        private const val TYPE_ACCESS = "ACCESS"
        private const val TYPE_REFRESH = "REFRESH"
    }
}
