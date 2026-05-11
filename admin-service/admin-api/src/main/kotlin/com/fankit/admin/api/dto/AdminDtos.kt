package com.fankit.admin.api.dto

import com.fankit.admin.domain.model.AdminDecisionLog
import com.fankit.admin.domain.model.ApprovalDecision
import com.fankit.admin.domain.model.ApprovalType
import com.fankit.admin.domain.model.StatisticsSummary
import com.fankit.admin.domain.model.TypeCount
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import java.time.Instant

data class ApproveRequest(
    @field:Min(1) val adminId: Long,    // TODO: JWT claim에서 추출하도록 추후 변경
)

data class RejectRequest(
    @field:Min(1) val adminId: Long,
    @field:NotBlank val reason: String,
)

data class DecisionResponse(
    val id: Long?,
    val type: ApprovalType,
    val targetId: String,
    val decision: ApprovalDecision,
    val decidedBy: Long,
    val reason: String?,
    val decidedAt: Instant?,
) {
    companion object {
        fun from(d: AdminDecisionLog) = DecisionResponse(
            d.id, d.type, d.targetId, d.decision, d.decidedBy, d.reason, d.decidedAt,
        )
    }
}

data class TypeCountResponse(val approved: Long, val rejected: Long, val total: Long) {
    companion object {
        fun from(c: TypeCount) = TypeCountResponse(c.approved, c.rejected, c.total)
    }
}

data class StatisticsResponse(
    val totalApproved: Long,
    val totalRejected: Long,
    val byType: Map<String, TypeCountResponse>,
) {
    companion object {
        fun from(s: StatisticsSummary) = StatisticsResponse(
            s.totalApproved,
            s.totalRejected,
            s.byType.mapKeys { it.key.name }.mapValues { TypeCountResponse.from(it.value) },
        )
    }
}
