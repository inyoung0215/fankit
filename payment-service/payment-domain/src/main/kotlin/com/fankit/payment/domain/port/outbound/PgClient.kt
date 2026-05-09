package com.fankit.payment.domain.port.outbound

import com.fankit.payment.domain.model.PaymentMethod

// PG사(카카오페이/토스 등) 호출 추상화
//
// 면접 어필: 이 인터페이스 위에 Resilience4j Circuit Breaker를 씌운다
//   - failureRateThreshold = 50% (10건 중 5건 실패 시 OPEN)
//   - waitDurationInOpenState = 30s (OPEN 후 30초 후 HALF_OPEN으로 시도)
//   - OPEN 상태에선 즉시 PgUnavailableException → Saga가 곧장 실패 처리
//
// 왜 50%/30s? PG사 일시 장애가 보통 분 단위. 너무 짧으면 정상 회복 전에 재시도 실패 누적.
//             너무 길면 사용자 결제 불가 시간이 길어짐. 30s는 카카오 결제팀 공개 가이드 참고치.
interface PgClient {
    fun approve(request: PgApprovalRequest): PgApprovalResponse
    fun cancel(pgTransactionId: String): PgCancelResponse
}

data class PgApprovalRequest(
    val orderId: Long,
    val amount: Int,
    val method: PaymentMethod,
)

data class PgApprovalResponse(
    val pgTransactionId: String,
    val approvedAmount: Int,
)

data class PgCancelResponse(
    val pgTransactionId: String,
    val cancelledAmount: Int,
)
