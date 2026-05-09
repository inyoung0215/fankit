package com.fankit.order.domain.port.outbound

import com.fankit.order.domain.model.Order

interface OrderRepository {
    fun save(order: Order): Order
    fun findById(id: Long): Order?
}
