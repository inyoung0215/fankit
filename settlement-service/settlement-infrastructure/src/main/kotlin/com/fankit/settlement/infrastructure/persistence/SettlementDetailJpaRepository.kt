package com.fankit.settlement.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface SettlementDetailJpaRepository : JpaRepository<SettlementDetailJpaEntity, Long> {
    fun findAllBySettlementId(settlementId: Long): List<SettlementDetailJpaEntity>
}
