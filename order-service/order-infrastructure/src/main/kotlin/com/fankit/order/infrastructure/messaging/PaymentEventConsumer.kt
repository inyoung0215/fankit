package com.fankit.order.infrastructure.messaging

import com.fankit.order.application.PaymentEventHandler
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

// Payment Service의 OutboxRelay가 발행한 이벤트를 consume
//
// at-least-once 의미: 같은 이벤트가 여러 번 도착할 수 있음 → 멱등 처리 필수
//   ConfirmOrderUseCase / CancelOrderUseCase가 이미 같은 상태면 no-op 처리
//
// groupId가 동일한 컨슈머 인스턴스가 여러 대여도 partition 분배되어 같은 메시지는 1대에만 도착
@Component
class PaymentEventConsumer(
    private val handler: PaymentEventHandler,
    private val objectMapper: ObjectMapper,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(
        topics = ["payment.completed"],
        groupId = "order-service",
        containerFactory = "stringKafkaListenerContainerFactory",
    )
    fun onPaymentCompleted(payload: String) {
        log.info("[Kafka] payment.completed 수신: {}", payload)
        val event = objectMapper.readValue<PaymentCompletedEvent>(payload)
        handler.onPaymentCompleted(event.orderId, event.paymentId)
    }

    @KafkaListener(
        topics = ["payment.failed"],
        groupId = "order-service",
        containerFactory = "stringKafkaListenerContainerFactory",
    )
    fun onPaymentFailed(payload: String) {
        log.info("[Kafka] payment.failed 수신: {}", payload)
        val event = objectMapper.readValue<PaymentFailedEvent>(payload)
        handler.onPaymentFailed(event.orderId)
    }
}
