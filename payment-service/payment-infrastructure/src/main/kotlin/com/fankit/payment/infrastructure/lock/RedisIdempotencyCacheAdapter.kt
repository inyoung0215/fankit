package com.fankit.payment.infrastructure.lock

import com.fankit.payment.domain.port.outbound.IdempotencyCache
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RedisIdempotencyCacheAdapter(
    private val redisTemplate: StringRedisTemplate,
) : IdempotencyCache {

    override fun put(key: String, paymentId: Long, ttlSeconds: Long) {
        redisTemplate.opsForValue().set(redisKey(key), paymentId.toString(), Duration.ofSeconds(ttlSeconds))
    }

    override fun get(key: String): Long? =
        redisTemplate.opsForValue().get(redisKey(key))?.toLong()

    private fun redisKey(key: String) = "idem:$key"
}
