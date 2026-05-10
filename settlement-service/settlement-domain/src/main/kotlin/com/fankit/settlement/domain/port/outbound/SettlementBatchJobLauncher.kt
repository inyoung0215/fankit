package com.fankit.settlement.domain.port.outbound

import java.time.LocalDate

// Spring Batch 같은 인프라 기술을 도메인이 모르도록 추상화
// (도메인은 "정산 배치를 실행하면 결과가 온다" 정도만 알면 됨)
interface SettlementBatchJobLauncher {
    fun runSettlementBatch(period: LocalDate): SettlementBatchResult
}

data class SettlementBatchResult(
    val processedRecordCount: Long,
    val settlementCount: Int,
    val totalNetAmount: Long,
)
