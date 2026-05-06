package com.fankit.user.infrastructure.redis

import com.fankit.user.domain.port.outbound.RefreshTokenStore
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RedisRefreshTokenStore(
    private val redisTemplate: StringRedisTemplate,
) : RefreshTokenStore {

    override fun save(userId: Long, refreshToken: String, ttlSeconds: Long) {
        redisTemplate.opsForValue().set(key(userId), refreshToken, Duration.ofSeconds(ttlSeconds))
    }

    override fun findByUserId(userId: Long): String? =
        redisTemplate.opsForValue().get(key(userId))

    override fun deleteByUserId(userId: Long) {
        redisTemplate.delete(key(userId))
    }

    // userId 1개당 토큰 1개 정책 — 다기기 동시 로그인 미허용 (단순화)
    // 다기기 허용 정책 필요 시 key를 "refresh:{userId}:{deviceId}"로 확장
    private fun key(userId: Long) = "refresh:$userId"
}
