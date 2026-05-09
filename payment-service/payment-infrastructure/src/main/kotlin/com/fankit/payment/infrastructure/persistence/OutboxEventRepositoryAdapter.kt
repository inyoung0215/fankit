package com.fankit.payment.infrastructure.persistence

import com.fankit.payment.domain.model.OutboxEvent
import com.fankit.payment.domain.model.OutboxStatus
import com.fankit.payment.domain.port.outbound.OutboxEventRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class OutboxEventRepositoryAdapter(
    private val jpaRepository: OutboxEventJpaRepository,
) : OutboxEventRepository {

    override fun save(event: OutboxEvent): OutboxEvent =
        jpaRepository.save(OutboxEventJpaEntity.fromDomain(event)).toDomain()

    override fun findPending(limit: Int): List<OutboxEvent> =
        jpaRepository.findAllByStatusOrderByIdAsc(OutboxStatus.PENDING, PageRequest.of(0, limit))
            .map { it.toDomain() }

    override fun markPublished(id: Long) {
        jpaRepository.findById(id).ifPresent {
            it.status = OutboxStatus.PUBLISHED
            it.publishedAt = Instant.now()
            jpaRepository.save(it)
        }
    }
}
