package com.fankit.settlement.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface SettlementJpaRepository : JpaRepository<SettlementJpaEntity, Long> {
    fun findByCreatorIdAndPeriod(creatorId: Long, period: LocalDate): SettlementJpaEntity?
    fun findAllByCreatorIdOrderByPeriodDesc(creatorId: Long): List<SettlementJpaEntity>
}
