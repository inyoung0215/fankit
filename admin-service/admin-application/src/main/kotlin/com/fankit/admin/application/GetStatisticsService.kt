package com.fankit.admin.application

import com.fankit.admin.domain.model.ApprovalDecision
import com.fankit.admin.domain.model.ApprovalType
import com.fankit.admin.domain.model.StatisticsSummary
import com.fankit.admin.domain.model.TypeCount
import com.fankit.admin.domain.port.inbound.GetStatisticsUseCase
import com.fankit.admin.domain.port.outbound.AdminDecisionLogRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetStatisticsService(
    private val logRepository: AdminDecisionLogRepository,
) : GetStatisticsUseCase {

    @Transactional(readOnly = true)
    override fun summary(): StatisticsSummary {
        val byType = ApprovalType.entries.associateWith { type ->
            TypeCount(
                approved = logRepository.countBy(type, ApprovalDecision.APPROVED),
                rejected = logRepository.countBy(type, ApprovalDecision.REJECTED),
            )
        }
        return StatisticsSummary(
            totalApproved = byType.values.sumOf { it.approved },
            totalRejected = byType.values.sumOf { it.rejected },
            byType = byType,
        )
    }
}
