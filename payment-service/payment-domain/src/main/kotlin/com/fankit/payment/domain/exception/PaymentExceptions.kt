package com.fankit.payment.domain.exception

import com.fankit.payment.domain.model.PaymentStatus

class PaymentNotFoundException(id: Long) :
    RuntimeException("결제를 찾을 수 없습니다: $id")

class IllegalPaymentStateException(from: PaymentStatus, to: PaymentStatus) :
    RuntimeException("결제 상태 전이 불가: $from → $to")

// Idempotency Key 재사용 시도 (이전 결과 반환 — 예외라기보단 정상 흐름의 분기)
class DuplicatePaymentRequestException(val existingPaymentId: Long) :
    RuntimeException("이미 처리된 결제 요청입니다: paymentId=$existingPaymentId")

// 동일 주문에 대한 중복 결제 시도 (비관적 락이 잡힌 후 재확인 단계에서 발견)
class OrderAlreadyPaidException(orderId: Long) :
    RuntimeException("이미 결제가 완료된 주문입니다: orderId=$orderId")

// PG 호출 실패 / Circuit Breaker OPEN
class PgUnavailableException(reason: String) :
    RuntimeException("PG사 호출 실패: $reason")
