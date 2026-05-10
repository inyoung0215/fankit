package com.fankit.settlement.domain.model

// 결제 1건당 정산 명세
// salesAmount = 결제 금액
// commissionAmount = 플랫폼 수수료
// netAmount = 크리에이터 지급 = salesAmount - commissionAmount
data class SettlementDetail(
    val id: Long? = null,
    val settlementId: Long? = null,
    val paymentId: Long,
    val goodsId: String,
    val salesAmount: Int,
    val commissionAmount: Int,
    val netAmount: Int,
) {
    init {
        require(salesAmount > 0) { "salesAmount > 0" }
        require(commissionAmount >= 0) { "commissionAmount >= 0" }
        require(netAmount == salesAmount - commissionAmount) {
            "netAmount 불일치: $salesAmount - $commissionAmount != $netAmount"
        }
    }

    companion object {
        fun calculate(
            paymentId: Long, goodsId: String, salesAmount: Int, commission: Commission,
        ): SettlementDetail {
            val commissionAmount = commission.applyTo(salesAmount)
            return SettlementDetail(
                paymentId = paymentId, goodsId = goodsId,
                salesAmount = salesAmount,
                commissionAmount = commissionAmount,
                netAmount = salesAmount - commissionAmount,
            )
        }
    }
}
