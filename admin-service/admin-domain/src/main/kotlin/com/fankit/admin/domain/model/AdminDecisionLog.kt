package com.fankit.admin.domain.model

import java.time.Instant

// Admin이 내린 승인/반려 결정의 영구 기록
//
// 외부 서비스(Goods/User)는 승인 결과 자체를 자기 도메인에 반영하지만,
// "누가 언제 어떤 결정을 내렸는가"의 audit trail은 Admin이 보관 — 회계/감사용
data class AdminDecisionLog(
    val id: Long? = null,
    val type: ApprovalType,
    val targetId: String,            // goodsId(MongoDB ObjectId) 또는 userId(Long.toString)
    val decision: ApprovalDecision,
    val decidedBy: Long,             // adminId
    val reason: String? = null,      // REJECTED 시 권장
    val decidedAt: Instant? = null,
) {
    init {
        require(targetId.isNotBlank()) { "targetId는 비어있을 수 없다" }
        if (decision == ApprovalDecision.REJECTED) {
            require(!reason.isNullOrBlank()) { "REJECTED는 사유가 필수" }
        }
    }
}
