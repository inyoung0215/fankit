package com.fankit.payment.infrastructure.persistence

import com.fankit.payment.domain.model.IdempotencyRecord
import com.fankit.payment.domain.port.outbound.IdempotencyKeyRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Repository

@Repository
class IdempotencyKeyRepositoryAdapter(
    private val jpaRepository: IdempotencyKeyJpaRepository,
) : IdempotencyKeyRepository {

    // unique constraint 위반 → null 반환 (caller가 race로 인지)
    override fun saveIfAbsent(record: IdempotencyRecord): IdempotencyRecord? = try {
        jpaRepository.save(IdempotencyKeyJpaEntity.fromDomain(record)).toDomain()
    } catch (e: DataIntegrityViolationException) {
        null
    }

    override fun findByKey(key: String): IdempotencyRecord? =
        jpaRepository.findByIdempotencyKey(key)?.toDomain()
}
