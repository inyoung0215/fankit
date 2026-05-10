package com.fankit.settlement.domain.model

import java.time.Instant
import java.time.LocalDate

// 대사 (Reconciliation)
//
// 정의: PG사가 매일 발송하는 정산 데이터 vs 내부 결제 데이터의 정합성 비교
//
// 왜 필요한가:
//   - PG사 일시 장애로 internal에선 APPROVED인데 PG에서 누락됐을 수 있음 (또는 그 반대)
//   - 금액 차이 발생 시 자동 알람 + 재대조 필요
//   - 자동 탐지 없이는 정산 잘못된 채로 송금 — 회계 사고
data class Reconciliation(
    val id: Long? = null,
    val period: LocalDate,
    val paymentId: Long,
    val internalAmount: Int?,   // 내부 결제 금액 (없으면 null)
    val pgAmount: Int?,         // PG 정산 데이터 금액 (없으면 null)
    val status: ReconciliationStatus,
    val checkedAt: Instant,
)

enum class ReconciliationStatus {
    MATCHED,            // 양쪽 모두 존재 + 금액 일치
    AMOUNT_MISMATCH,    // 양쪽 모두 존재하지만 금액 다름 (운영 개입 필수)
    MISSING_INTERNAL,   // PG에는 있는데 내부에 없음 (결제 누락 또는 사기 의심)
    MISSING_PG,         // 내부에는 있는데 PG 정산엔 없음 (PG 측 누락)
}
