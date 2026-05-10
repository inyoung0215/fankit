package com.fankit.settlement.domain.port.inbound

import java.time.LocalDate

interface RunSettlementUseCase {
    // 수동 트리거 (운영 도구용 또는 정합성 재처리)
    // Quartz 스케줄러도 내부적으로 이 use case를 호출
    fun run(period: LocalDate): RunSettlementResult
}

data class RunSettlementResult(
    val period: LocalDate,
    val processedRecordCount: Long,
    val settlementCount: Int,
    val totalNetAmount: Long,
)
