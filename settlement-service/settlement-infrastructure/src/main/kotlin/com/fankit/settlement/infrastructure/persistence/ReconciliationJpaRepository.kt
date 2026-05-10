package com.fankit.settlement.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface ReconciliationJpaRepository : JpaRepository<ReconciliationJpaEntity, Long> {
    fun findAllByPeriod(period: LocalDate): List<ReconciliationJpaEntity>
}
