package com.fankit.payment.domain.model

import java.time.Instant

// Idempotency Key 영구 저장 record
// (Redis는 캐시, MySQL이 SoT — 캐시 만료/유실에도 중복 차단 보장)
data class IdempotencyRecord(
    val key: String,
    val paymentId: Long,
    val createdAt: Instant? = null,
)
