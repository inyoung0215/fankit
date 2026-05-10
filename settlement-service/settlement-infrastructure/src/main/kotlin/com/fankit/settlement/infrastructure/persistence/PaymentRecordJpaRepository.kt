package com.fankit.settlement.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant

interface PaymentRecordJpaRepository : JpaRepository<PaymentRecordJpaEntity, Long> {

    @Query(
        """
        SELECT p FROM PaymentRecordJpaEntity p
        WHERE p.settled = false AND p.approvedAt >= :from AND p.approvedAt < :to
        ORDER BY p.paymentId ASC
        """
    )
    fun findUnsettledBetween(
        @Param("from") from: Instant, @Param("to") to: Instant,
        pageable: org.springframework.data.domain.Pageable,
    ): List<PaymentRecordJpaEntity>

    @Query(
        """
        SELECT COUNT(p) FROM PaymentRecordJpaEntity p
        WHERE p.settled = false AND p.approvedAt >= :from AND p.approvedAt < :to
        """
    )
    fun countUnsettledBetween(@Param("from") from: Instant, @Param("to") to: Instant): Long

    @Modifying
    @Query("UPDATE PaymentRecordJpaEntity p SET p.settled = true WHERE p.paymentId IN :ids")
    fun markSettled(@Param("ids") ids: List<Long>): Int
}
