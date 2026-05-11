package com.fankit.admin.domain.port.outbound

import com.fankit.admin.domain.model.AdminDecisionLog
import com.fankit.admin.domain.model.ApprovalDecision
import com.fankit.admin.domain.model.ApprovalType

interface AdminDecisionLogRepository {
    fun save(log: AdminDecisionLog): AdminDecisionLog
    fun findAll(type: ApprovalType?, page: Int, size: Int): List<AdminDecisionLog>
    fun countBy(type: ApprovalType?, decision: ApprovalDecision): Long
}
