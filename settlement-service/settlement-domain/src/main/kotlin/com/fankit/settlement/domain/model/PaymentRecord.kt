package com.fankit.settlement.domain.model

import java.time.Instant

// Settlement Service 자체 테이블에 적재되는 결제 기록
//
// 데이터 출처: Kafka payment.completed 이벤트 (Settlement Service의 컨슈머가 적재)
//
// 왜 자체 테이블에?
//   - DB-per-service 원칙 유지 (Payment의 DB를 직접 read하지 않는다)
//   - 정산 배치는 read-heavy + 새로운 인덱스 필요 (settled flag, approved_at range 등)
//   - Payment 서비스의 운영 부담을 정산 배치가 침범하지 않게 분리
data class PaymentRecord(
    val paymentId: Long,
    val creatorId: Long,
    val goodsId: String,
    val amount: Int,
    val approvedAt: Instant,
    val settled: Boolean = false,    // 정산 처리 완료 여부 — 배치 멱등성 보장
)
