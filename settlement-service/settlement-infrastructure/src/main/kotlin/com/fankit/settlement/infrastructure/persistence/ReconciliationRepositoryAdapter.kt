package com.fankit.settlement.infrastructure.persistence

import com.fankit.settlement.domain.model.Reconciliation
import com.fankit.settlement.domain.port.outbound.ReconciliationRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class ReconciliationRepositoryAdapter(
    private val jpaRepository: ReconciliationJpaRepository,
) : ReconciliationRepository {

    override fun saveAll(items: List<Reconciliation>): List<Reconciliation> =
        jpaRepository.saveAll(items.map { ReconciliationJpaEntity.fromDomain(it) }).map { it.toDomain() }

    override fun findAllByPeriod(period: LocalDate): List<Reconciliation> =
        jpaRepository.findAllByPeriod(period).map { it.toDomain() }
}
