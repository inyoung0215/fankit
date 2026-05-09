package com.fankit.order.application

import com.fankit.order.domain.model.Order
import com.fankit.order.domain.model.OrderItem
import com.fankit.order.domain.port.inbound.CreateOrderCommand
import com.fankit.order.domain.port.inbound.CreateOrderUseCase
import com.fankit.order.domain.port.outbound.OrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CreateOrderService(
    private val orderRepository: OrderRepository,
) : CreateOrderUseCase {

    @Transactional
    override fun create(command: CreateOrderCommand): Order {
        val items = command.items.map {
            OrderItem(it.goodsId, it.goodsName, it.quantity, it.unitPrice)
        }
        val order = Order.create(command.userId, items, command.shippingAddress)
        return orderRepository.save(order)
    }
}
