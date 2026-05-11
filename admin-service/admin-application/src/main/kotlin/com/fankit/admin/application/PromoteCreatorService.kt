package com.fankit.admin.application

import com.fankit.admin.domain.model.AdminDecisionLog
import com.fankit.admin.domain.model.ApprovalDecision
import com.fankit.admin.domain.model.ApprovalType
import com.fankit.admin.domain.port.inbound.PromoteCreatorUseCase
import com.fankit.admin.domain.port.outbound.AdminDecisionLogRepository
import com.fankit.admin.domain.port.outbound.UserAdminClient
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PromoteCreatorService(
    private val userAdminClient: UserAdminClient,
    private val logRepository: AdminDecisionLogRepository,
) : PromoteCreatorUseCase {

    @Transactional
    override fun promote(userId: Long, adminId: Long): AdminDecisionLog {
        userAdminClient.promoteToCreator(userId)
        return logRepository.save(
            AdminDecisionLog(
                type = ApprovalType.CREATOR_TRANSITION, targetId = userId.toString(),
                decision = ApprovalDecision.APPROVED, decidedBy = adminId,
            )
        )
    }
}
