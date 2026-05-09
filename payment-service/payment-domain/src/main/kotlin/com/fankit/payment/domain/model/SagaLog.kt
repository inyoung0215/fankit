package com.fankit.payment.domain.model

import java.time.Instant

// Saga 진행 상태를 영속화하는 로그
//
// 면접 어필: Choreography 대신 Orchestration을 선택한 핵심 근거 = "운영 가시성"
// SagaLog 한 테이블에 모든 진행/실패 정보가 모이므로
// "어느 step에서 왜 멈췄나"를 한 쿼리로 진단 가능
data class SagaLog(
    val id: Long? = null,
    val sagaId: String,                    // UUID, X-Idempotency-Key와 다른 saga 인스턴스 식별자
    val paymentId: Long? = null,
    val status: SagaStatus,
    val currentStep: SagaStep,
    val failureReason: String? = null,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
)

enum class SagaStatus {
    STARTED,                // saga 시작
    COMPLETED,              // 모든 step 성공
    COMPENSATING,           // step 실패 → 보상 진행 중
    COMPENSATED,            // 보상 완료
    COMPENSATION_FAILED,    // 보상도 실패 — 운영팀 수동 개입 필요
}

enum class SagaStep {
    PAYMENT_APPROVE,    // step 1: PG 승인
    ORDER_CONFIRM,      // step 2: 주문 상태 PAID 전이
    STOCK_DECREASE,     // step 3: 재고 차감 (분산 락)
    DONE,               // 모두 성공
}
