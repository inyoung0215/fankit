package com.fankit.order.domain.port.inbound

import com.fankit.order.domain.model.Order

interface CreateOrderUseCase {
    fun create(command: CreateOrderCommand): Order
}

data class CreateOrderCommand(
    val userId: Long,
    val items: List<CreateOrderItemCommand>,
    val shippingAddress: String,
)

data class CreateOrderItemCommand(
    val goodsId: String,
    val goodsName: String,
    val quantity: Int,
    val unitPrice: Int,
)
