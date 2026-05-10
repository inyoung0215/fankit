package com.fankit.settlement.infrastructure.persistence

import com.fankit.settlement.domain.model.Settlement
import com.fankit.settlement.domain.port.outbound.SettlementRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class SettlementRepositoryAdapter(
    private val jpaRepository: SettlementJpaRepository,
) : SettlementRepository {

    override fun save(settlement: Settlement): Settlement =
        jpaRepository.save(SettlementJpaEntity.fromDomain(settlement)).toDomain()

    override fun findById(id: Long): Settlement? =
        jpaRepository.findById(id).orElse(null)?.toDomain()

    override fun findByCreatorIdAndPeriod(creatorId: Long, period: LocalDate): Settlement? =
        jpaRepository.findByCreatorIdAndPeriod(creatorId, period)?.toDomain()

    override fun findAllByCreatorId(creatorId: Long): List<Settlement> =
        jpaRepository.findAllByCreatorIdOrderByPeriodDesc(creatorId).map { it.toDomain() }
}
