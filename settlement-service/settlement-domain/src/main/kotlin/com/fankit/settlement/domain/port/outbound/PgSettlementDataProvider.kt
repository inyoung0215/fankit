package com.fankit.settlement.domain.port.outbound

import java.time.LocalDate

// PG사가 매일 발송하는 정산 데이터 — 대사용
//
// 실전 형태: PG가 매일 새벽 SFTP/S3에 CSV 업로드 → 우리는 그걸 읽어 파싱
// 여기선 Mock 구현 (의도적으로 일부 데이터를 mismatch/missing 처리해서 대사 동작 시연)
interface PgSettlementDataProvider {
    fun fetch(period: LocalDate): List<PgSettlementItem>
}

data class PgSettlementItem(
    val pgTransactionId: String,
    val paymentId: Long,
    val amount: Int,
)
