package com.fankit.admin.infrastructure.persistence

import com.fankit.admin.domain.model.AdminDecisionLog
import com.fankit.admin.domain.model.ApprovalDecision
import com.fankit.admin.domain.model.ApprovalType
import com.fankit.admin.domain.port.outbound.AdminDecisionLogRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository

@Repository
class AdminDecisionLogRepositoryAdapter(
    private val jpaRepository: AdminDecisionLogJpaRepository,
) : AdminDecisionLogRepository {

    override fun save(log: AdminDecisionLog): AdminDecisionLog =
        jpaRepository.save(AdminDecisionLogJpaEntity.fromDomain(log)).toDomain()

    override fun findAll(type: ApprovalType?, page: Int, size: Int): List<AdminDecisionLog> {
        val pageable = PageRequest.of(page, size)
        val rows = if (type == null) {
            jpaRepository.findAllByOrderByIdDesc(pageable)
        } else {
            jpaRepository.findAllByTypeOrderByIdDesc(type, pageable)
        }
        return rows.map { it.toDomain() }
    }

    override fun countBy(type: ApprovalType?, decision: ApprovalDecision): Long =
        if (type == null) jpaRepository.countByDecision(decision)
        else jpaRepository.countByTypeAndDecision(type, decision)
}
