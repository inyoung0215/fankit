package com.fankit.payment.domain.port.outbound

import com.fankit.payment.domain.model.IdempotencyRecord

// Idempotency Key 저장 — DB unique index가 SoT
//
// 이중 체크 전략:
//   1차) Redis에서 빠르게 조회 (RedisIdempotencyCache, 별도 port)
//   2차) DB unique constraint (race condition 시 최후 보루)
//        save() 시 unique 위반 → 동시 요청 중 1건만 통과
interface IdempotencyKeyRepository {
    fun saveIfAbsent(record: IdempotencyRecord): IdempotencyRecord?  // 중복이면 null 반환
    fun findByKey(key: String): IdempotencyRecord?
}
