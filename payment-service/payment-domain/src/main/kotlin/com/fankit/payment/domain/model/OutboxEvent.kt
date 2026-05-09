package com.fankit.payment.domain.model

import java.time.Instant

// Transactional Outbox Pattern
//
// 문제: DB 트랜잭션 내에서 Kafka.publish() 직접 호출하면
//       Kafka 발행은 성공 + DB rollback → 실제 결제는 안됐는데 'payment.completed' 이벤트만 흘러감
//
// 해법: 같은 DB 트랜잭션에서 Payment INSERT + OutboxEvent INSERT를 원자적으로
//       별도 OutboxRelay (스케줄러)가 PENDING 이벤트를 읽어 Kafka 발행 후 PUBLISHED로 표시
//       → 'DB commit + Kafka 발행' 순서가 항상 일관됨
data class OutboxEvent(
    val id: Long? = null,
    val aggregateType: String,    // "Payment"
    val aggregateId: String,      // payment.id (문자열)
    val eventType: String,        // "payment.completed", "payment.failed"
    val payload: String,          // JSON 직렬화된 이벤트 본문
    val status: OutboxStatus = OutboxStatus.PENDING,
    val createdAt: Instant? = null,
    val publishedAt: Instant? = null,
)

enum class OutboxStatus {
    PENDING,    // DB에 적재됨, 아직 Kafka 미발행
    PUBLISHED,  // Kafka 발행 완료
}
