package com.fankit.order.domain.port.inbound

// Saga 보상(POST /orders/{id}/cancel) 또는 Kafka payment.failed 컨슈머가 호출
// 멱등성 보장: 이미 CANCELLED면 no-op
interface CancelOrderUseCase {
    fun cancel(orderId: Long)
}
