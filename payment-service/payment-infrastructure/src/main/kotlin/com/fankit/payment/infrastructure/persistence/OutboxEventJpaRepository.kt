package com.fankit.payment.infrastructure.persistence

import com.fankit.payment.domain.model.OutboxStatus
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface OutboxEventJpaRepository : JpaRepository<OutboxEventJpaEntity, Long> {
    fun findAllByStatusOrderByIdAsc(status: OutboxStatus, pageable: Pageable): List<OutboxEventJpaEntity>
}
