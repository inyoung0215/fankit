package com.fankit.user.domain.port.outbound

// Refresh Token을 외부 저장소(Redis)에 보관하기 위한 port
// 저장 이유: Access Token만 stateless로 두고, Refresh Token은 서버가 통제 가능해야
//          탈취 시 즉시 무효화(Redis key 삭제)할 수 있다
interface RefreshTokenStore {
    fun save(userId: Long, refreshToken: String, ttlSeconds: Long)
    fun findByUserId(userId: Long): String?
    fun deleteByUserId(userId: Long)
}
