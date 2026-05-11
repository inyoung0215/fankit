package com.fankit.admin.application

import com.fankit.admin.domain.model.AdminDecisionLog
import com.fankit.admin.domain.model.ApprovalDecision
import com.fankit.admin.domain.model.ApprovalType
import com.fankit.admin.domain.port.inbound.ApproveGoodsUseCase
import com.fankit.admin.domain.port.outbound.AdminDecisionLogRepository
import com.fankit.admin.domain.port.outbound.GoodsAdminClient
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

// 외부 호출 → 로그 적재 순서
//   외부 호출이 실패하면 예외 전파, 로그 적재 안 됨 (트랜잭션 일관성)
//   성공 후 로그 저장 실패 = 외부엔 반영됐지만 audit 누락 → 운영 알람 필요
//
// 더 강한 보장이 필요하면 Outbox 패턴 또는 Saga 적용. 현재는 단순화.
@Service
class ApproveGoodsService(
    private val goodsAdminClient: GoodsAdminClient,
    private val logRepository: AdminDecisionLogRepository,
) : ApproveGoodsUseCase {

    @Transactional
    override fun approve(goodsId: String, adminId: Long): AdminDecisionLog {
        goodsAdminClient.approveGoods(goodsId)
        return logRepository.save(
            AdminDecisionLog(
                type = ApprovalType.GOODS, targetId = goodsId,
                decision = ApprovalDecision.APPROVED, decidedBy = adminId,
            )
        )
    }
}
