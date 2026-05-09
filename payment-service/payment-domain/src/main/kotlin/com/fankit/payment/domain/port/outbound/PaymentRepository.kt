package com.fankit.payment.domain.port.outbound

import com.fankit.payment.domain.model.Payment

interface PaymentRepository {
    fun save(payment: Payment): Payment
    fun findById(id: Long): Payment?
    fun findByOrderId(orderId: Long): Payment?

    // 비관적 락 (SELECT FOR UPDATE) — 동일 주문 동시 결제 차단
    // 첫 트랜잭션이 lock 잡고 commit 후, 두 번째는 already-PAID 보고 거절
    fun findByOrderIdForUpdate(orderId: Long): Payment?
}
