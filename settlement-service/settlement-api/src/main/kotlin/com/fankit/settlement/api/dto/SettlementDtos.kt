package com.fankit.settlement.api.dto

import com.fankit.settlement.domain.model.Reconciliation
import com.fankit.settlement.domain.model.Settlement
import com.fankit.settlement.domain.model.SettlementDetail
import com.fankit.settlement.domain.model.SettlementStatus
import com.fankit.settlement.domain.port.inbound.ReconciliationSummary
import com.fankit.settlement.domain.port.inbound.RunSettlementResult
import com.fankit.settlement.domain.port.inbound.SettlementWithDetails
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import java.time.Instant
import java.time.LocalDate

data class RunSettlementResponse(
    val period: LocalDate,
    val processedRecordCount: Long,
    val settlementCount: Int,
    val totalNetAmount: Long,
) {
    companion object {
        fun from(r: RunSettlementResult) = RunSettlementResponse(
            r.period, r.processedRecordCount, r.settlementCount, r.totalNetAmount,
        )
    }
}

data class SettlementResponse(
    val id: Long,
    val creatorId: Long,
    val period: LocalDate,
    val totalSales: Long,
    val totalCommission: Long,
    val netAmount: Long,
    val status: SettlementStatus,
) {
    companion object {
        fun from(s: Settlement) = SettlementResponse(
            s.id!!, s.creatorId, s.period, s.totalSales, s.totalCommission, s.netAmount, s.status,
        )
    }
}

data class SettlementDetailResponse(
    val id: Long?,
    val paymentId: Long,
    val goodsId: String,
    val salesAmount: Int,
    val commissionAmount: Int,
    val netAmount: Int,
) {
    companion object {
        fun from(d: SettlementDetail) = SettlementDetailResponse(
            d.id, d.paymentId, d.goodsId, d.salesAmount, d.commissionAmount, d.netAmount,
        )
    }
}

data class SettlementWithDetailsResponse(
    val settlement: SettlementResponse,
    val details: List<SettlementDetailResponse>,
) {
    companion object {
        fun from(w: SettlementWithDetails) = SettlementWithDetailsResponse(
            SettlementResponse.from(w.settlement),
            w.details.map(SettlementDetailResponse::from),
        )
    }
}

data class ReconciliationSummaryResponse(
    val period: LocalDate,
    val totalChecked: Int,
    val countByStatus: Map<String, Int>,
) {
    companion object {
        fun from(s: ReconciliationSummary) = ReconciliationSummaryResponse(
            s.period, s.totalChecked, s.countByStatus.mapKeys { it.key.name },
        )
    }
}

data class ReconciliationResponse(
    val id: Long?,
    val period: LocalDate,
    val paymentId: Long,
    val internalAmount: Int?,
    val pgAmount: Int?,
    val status: String,
    val checkedAt: Instant,
) {
    companion object {
        fun from(r: Reconciliation) = ReconciliationResponse(
            r.id, r.period, r.paymentId, r.internalAmount, r.pgAmount, r.status.name, r.checkedAt,
        )
    }
}

// 시연용: PaymentRecord 외부에서 INSERT (실전엔 Kafka payment.completed 컨슈머가 적재)
data class PaymentRecordRequest(
    @field:Min(1) val paymentId: Long,
    @field:Min(1) val creatorId: Long,
    @field:NotBlank val goodsId: String,
    @field:Min(1) val amount: Int,
    val approvedAt: Instant = Instant.now(),
)
