package com.fankit.order.domain.port.inbound

import com.fankit.order.domain.model.Order

interface GetOrderUseCase {
    fun getById(id: Long): Order
}
