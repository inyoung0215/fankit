package com.fankit.payment.infrastructure.persistence

import com.fankit.payment.domain.model.SagaLog
import com.fankit.payment.domain.port.outbound.SagaLogRepository
import org.springframework.stereotype.Repository

@Repository
class SagaLogRepositoryAdapter(
    private val jpaRepository: SagaLogJpaRepository,
) : SagaLogRepository {

    override fun save(log: SagaLog): SagaLog =
        jpaRepository.save(SagaLogJpaEntity.fromDomain(log)).toDomain()

    override fun findBySagaId(sagaId: String): SagaLog? =
        jpaRepository.findFirstBySagaIdOrderByIdDesc(sagaId)?.toDomain()
}
