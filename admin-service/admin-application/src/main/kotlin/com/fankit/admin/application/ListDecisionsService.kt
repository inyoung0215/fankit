package com.fankit.admin.application

import com.fankit.admin.domain.model.AdminDecisionLog
import com.fankit.admin.domain.model.ApprovalType
import com.fankit.admin.domain.port.inbound.ListDecisionsUseCase
import com.fankit.admin.domain.port.outbound.AdminDecisionLogRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ListDecisionsService(
    private val logRepository: AdminDecisionLogRepository,
) : ListDecisionsUseCase {

    @Transactional(readOnly = true)
    override fun list(type: ApprovalType?, page: Int, size: Int): List<AdminDecisionLog> {
        require(page >= 0) { "page는 0 이상" }
        require(size in 1..200) { "size는 1~200 사이" }
        return logRepository.findAll(type, page, size)
    }
}
