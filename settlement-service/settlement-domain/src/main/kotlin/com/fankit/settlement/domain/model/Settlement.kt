package com.fankit.settlement.domain.model

import java.time.LocalDate

// Settlement aggregate — 크리에이터별 일자별 정산 집계
//
// (creatorId, period) unique — 같은 일자에 대한 중복 정산 방지
data class Settlement(
    val id: Long? = null,
    val creatorId: Long,
    val period: LocalDate,           // 정산 대상 일자 (보통 전일)
    val totalSales: Long,            // 총 매출
    val totalCommission: Long,       // 총 수수료
    val netAmount: Long,             // 순지급액 = totalSales - totalCommission
    val status: SettlementStatus,
) {
    init {
        require(netAmount == totalSales - totalCommission) {
            "netAmount 불일치"
        }
    }

    fun complete(): Settlement = copy(status = SettlementStatus.COMPLETED)
    fun fail(): Settlement = copy(status = SettlementStatus.FAILED)

    companion object {
        fun aggregate(
            creatorId: Long,
            period: LocalDate,
            details: List<SettlementDetail>,
        ): Settlement {
            require(details.isNotEmpty()) { "정산 명세는 최소 1건 이상이어야 한다" }
            val totalSales = details.sumOf { it.salesAmount }.toLong()
            val totalCommission = details.sumOf { it.commissionAmount }.toLong()
            return Settlement(
                creatorId = creatorId, period = period,
                totalSales = totalSales, totalCommission = totalCommission,
                netAmount = totalSales - totalCommission,
                status = SettlementStatus.PROCESSING,
            )
        }
    }
}
