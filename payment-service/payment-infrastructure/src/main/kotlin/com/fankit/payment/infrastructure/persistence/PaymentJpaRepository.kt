package com.fankit.payment.infrastructure.persistence

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PaymentJpaRepository : JpaRepository<PaymentJpaEntity, Long> {
    fun findByOrderId(orderId: Long): PaymentJpaEntity?

    // 비관적 쓰기 락 — SELECT ... FROM payments WHERE order_id = ? FOR UPDATE
    // 동일 orderId 동시 결제 요청 직렬화
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PaymentJpaEntity p WHERE p.orderId = :orderId")
    fun findByOrderIdForUpdate(@Param("orderId") orderId: Long): PaymentJpaEntity?
}
