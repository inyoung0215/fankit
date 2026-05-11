package com.fankit.admin.domain.port.inbound

import com.fankit.admin.domain.model.AdminDecisionLog

interface PromoteCreatorUseCase {
    fun promote(userId: Long, adminId: Long): AdminDecisionLog
}
