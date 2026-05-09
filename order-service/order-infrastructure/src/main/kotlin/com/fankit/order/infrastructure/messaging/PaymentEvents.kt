package com.fankit.order.infrastructure.messaging

// Payment Service의 OutboxEvent payload 스키마와 일치
// (Avro/Schema Registry 도입 전까지 양쪽 서비스의 DTO를 수동으로 동기화)
data class PaymentCompletedEvent(
    val paymentId: Long,
    val orderId: Long,
    val userId: Long,
    val amount: Int,
    val pgTransactionId: String,
)

data class PaymentFailedEvent(
    val paymentId: Long,
    val orderId: Long,
    val userId: Long,
    val reason: String,
)
