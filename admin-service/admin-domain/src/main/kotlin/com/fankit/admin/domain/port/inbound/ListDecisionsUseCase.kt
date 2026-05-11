package com.fankit.admin.domain.port.inbound

import com.fankit.admin.domain.model.AdminDecisionLog
import com.fankit.admin.domain.model.ApprovalType

interface ListDecisionsUseCase {
    fun list(type: ApprovalType?, page: Int, size: Int): List<AdminDecisionLog>
}
