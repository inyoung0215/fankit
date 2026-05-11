package com.fankit.admin.application

import com.fankit.admin.domain.model.AdminDecisionLog
import com.fankit.admin.domain.model.ApprovalDecision
import com.fankit.admin.domain.model.ApprovalType
import com.fankit.admin.domain.port.inbound.RejectGoodsUseCase
import com.fankit.admin.domain.port.outbound.AdminDecisionLogRepository
import com.fankit.admin.domain.port.outbound.GoodsAdminClient
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RejectGoodsService(
    private val goodsAdminClient: GoodsAdminClient,
    private val logRepository: AdminDecisionLogRepository,
) : RejectGoodsUseCase {

    @Transactional
    override fun reject(goodsId: String, adminId: Long, reason: String): AdminDecisionLog {
        goodsAdminClient.rejectGoods(goodsId, reason)
        return logRepository.save(
            AdminDecisionLog(
                type = ApprovalType.GOODS, targetId = goodsId,
                decision = ApprovalDecision.REJECTED, decidedBy = adminId, reason = reason,
            )
        )
    }
}
