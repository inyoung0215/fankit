package com.fankit.settlement.infrastructure.batch

// 배치 내부 DTO — 도메인 SettlementDetail은 creatorId 필드가 없어
// Aggregate Step에서 group by가 안 되므로 인프라 전용으로 creatorId를 보존
data class SettlementBatchItem(
    val paymentId: Long,
    val creatorId: Long,
    val goodsId: String,
    val salesAmount: Int,
    val commissionAmount: Int,
    val netAmount: Int,
)
