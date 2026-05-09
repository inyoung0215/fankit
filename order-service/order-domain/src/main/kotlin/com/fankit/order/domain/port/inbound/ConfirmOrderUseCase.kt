package com.fankit.order.domain.port.inbound

// Saga의 동기 RPC(POST /orders/{id}/confirm)와 Kafka payment.completed 컨슈머 양쪽이 호출
// 멱등성 보장: 이미 PAID 상태면 no-op
interface ConfirmOrderUseCase {
    fun confirm(orderId: Long, paymentId: Long)
}
