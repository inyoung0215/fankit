package com.fankit.payment.domain.model

// Payment 상태 머신
//
//          ┌──── PG 승인 OK ────▶ APPROVED ──refund()──▶ REFUNDED
//          │                          ↑
//   PENDING ──── PG 거절 ─────▶ FAILED
//          │
//          └──── 사용자 취소 ──▶ CANCELLED
//
enum class PaymentStatus {
    PENDING,    // 결제 row 생성, PG 호출 전 (Idempotency 체크 통과 직후)
    APPROVED,   // PG 승인 완료, Saga 정상 종료
    FAILED,     // PG 거절 또는 Saga 보상 완료
    CANCELLED,  // 사용자/시스템이 PENDING 단계에서 취소
    REFUNDED,   // 환불 완료
}
