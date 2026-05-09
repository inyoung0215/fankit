package com.fankit.payment.domain.port.outbound

// Redis 기반 Idempotency 빠른 조회 캐시 (TTL 24h)
// DB hit을 절약하기 위한 1차 방어선 — SoT는 IdempotencyKeyRepository (MySQL)
interface IdempotencyCache {
    fun put(key: String, paymentId: Long, ttlSeconds: Long)
    fun get(key: String): Long?
}
