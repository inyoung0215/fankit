package com.fankit.order.application

import com.fankit.order.domain.exception.OrderNotFoundException
import com.fankit.order.domain.model.OrderStatus
import com.fankit.order.domain.port.inbound.CancelOrderUseCase
import com.fankit.order.domain.port.outbound.OrderRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CancelOrderService(
    private val orderRepository: OrderRepository,
) : CancelOrderUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun cancel(orderId: Long) {
        val order = orderRepository.findById(orderId) ?: throw OrderNotFoundException(orderId)
        if (order.status == OrderStatus.CANCELLED) {
            log.info("Order {} 이미 CANCELLED — cancel 멱등 skip", orderId)
            return
        }
        orderRepository.save(order.cancel())
    }
}
