package com.fankit.order.application

import com.fankit.order.domain.exception.OrderNotFoundException
import com.fankit.order.domain.model.Order
import com.fankit.order.domain.port.inbound.GetOrderUseCase
import com.fankit.order.domain.port.outbound.OrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetOrderService(
    private val orderRepository: OrderRepository,
) : GetOrderUseCase {

    @Transactional(readOnly = true)
    override fun getById(id: Long): Order =
        orderRepository.findById(id) ?: throw OrderNotFoundException(id)
}
