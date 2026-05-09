package com.fankit.order.application

import com.fankit.order.domain.exception.OrderNotFoundException
import com.fankit.order.domain.model.OrderStatus
import com.fankit.order.domain.port.inbound.ConfirmOrderUseCase
import com.fankit.order.domain.port.outbound.OrderRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ConfirmOrderService(
    private val orderRepository: OrderRepository,
) : ConfirmOrderUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    // 멱등 보장: 이미 PAID면 no-op (Kafka at-least-once 재전송 + Saga 동기 RPC 양립 가능)
    @Transactional
    override fun confirm(orderId: Long, paymentId: Long) {
        val order = orderRepository.findById(orderId) ?: throw OrderNotFoundException(orderId)
        if (order.status == OrderStatus.PAID) {
            log.info("Order {} 이미 PAID — confirm 멱등 skip", orderId)
            return
        }
        orderRepository.save(order.markPaid(paymentId))
    }
}
