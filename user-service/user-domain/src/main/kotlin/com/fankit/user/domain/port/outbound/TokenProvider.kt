package com.fankit.user.domain.port.outbound

import com.fankit.user.domain.model.Role

// JWT/Paseto 등 구체 토큰 기술을 도메인이 알 필요 없도록 port 분리
interface TokenProvider {
    fun issueAccessToken(userId: Long, role: Role): String
    fun issueRefreshToken(userId: Long): String

    // 유효하지 않으면 예외 발생 (서명 불일치, 만료, 형식 오류)
    fun parseUserId(token: String): Long
    fun parseRole(token: String): Role
}
