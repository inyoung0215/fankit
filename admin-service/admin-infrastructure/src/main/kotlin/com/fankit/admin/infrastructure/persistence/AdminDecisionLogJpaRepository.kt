package com.fankit.admin.infrastructure.persistence

import com.fankit.admin.domain.model.ApprovalDecision
import com.fankit.admin.domain.model.ApprovalType
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface AdminDecisionLogJpaRepository : JpaRepository<AdminDecisionLogJpaEntity, Long> {
    fun findAllByOrderByIdDesc(pageable: Pageable): List<AdminDecisionLogJpaEntity>
    fun findAllByTypeOrderByIdDesc(type: ApprovalType, pageable: Pageable): List<AdminDecisionLogJpaEntity>

    fun countByDecision(decision: ApprovalDecision): Long
    fun countByTypeAndDecision(type: ApprovalType, decision: ApprovalDecision): Long
}
