package com.fankit.settlement.infrastructure.persistence

import com.fankit.settlement.domain.model.PaymentRecord
import com.fankit.settlement.domain.port.outbound.PaymentRecordRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class PaymentRecordRepositoryAdapter(
    private val jpaRepository: PaymentRecordJpaRepository,
) : PaymentRecordRepository {

    override fun save(record: PaymentRecord): PaymentRecord =
        jpaRepository.save(PaymentRecordJpaEntity.fromDomain(record)).toDomain()

    override fun findUnsettledBetween(from: Instant, to: Instant, limit: Int, offset: Int): List<PaymentRecord> {
        val page = if (limit > 0) offset / limit else 0
        return jpaRepository.findUnsettledBetween(from, to, PageRequest.of(page, maxOf(limit, 1)))
            .map { it.toDomain() }
    }

    override fun findUnsettledAfter(from: Instant, to: Instant, afterPaymentId: Long, limit: Int): List<PaymentRecord> =
        jpaRepository.findUnsettledAfter(from, to, afterPaymentId, PageRequest.of(0, maxOf(limit, 1)))
            .map { it.toDomain() }

    override fun countUnsettledBetween(from: Instant, to: Instant): Long =
        jpaRepository.countUnsettledBetween(from, to)

    override fun markSettled(paymentIds: List<Long>) {
        if (paymentIds.isNotEmpty()) jpaRepository.markSettled(paymentIds)
    }
}
