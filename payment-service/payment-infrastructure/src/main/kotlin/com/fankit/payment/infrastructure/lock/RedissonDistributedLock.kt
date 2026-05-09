package com.fankit.payment.infrastructure.lock

import com.fankit.payment.domain.port.outbound.DistributedLock
import org.redisson.api.RedissonClient
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.concurrent.TimeUnit

// Redisson 기반 분산 락
//
// tryLock(waitTime, leaseTime, unit):
//   waitTime  = 락 획득 대기 시간 (초과 시 false)
//   leaseTime = 락을 자동 해제하는 시간 (서비스 죽어도 deadlock 방지)
//
// finally에서 unlock — 락 보유자만 unlock 가능 (Lua script로 atomic 검증)
@Component
class RedissonDistributedLock(
    private val redissonClient: RedissonClient,
) : DistributedLock {

    override fun <T> withLock(key: String, waitTime: Duration, leaseTime: Duration, action: () -> T): T {
        val lock = redissonClient.getLock(key)
        val acquired = lock.tryLock(waitTime.toMillis(), leaseTime.toMillis(), TimeUnit.MILLISECONDS)
        if (!acquired) throw IllegalStateException("분산 락 획득 실패: $key")
        try {
            return action()
        } finally {
            if (lock.isHeldByCurrentThread) lock.unlock()
        }
    }
}
