package com.fankit.order.domain.model

import com.fankit.order.domain.exception.IllegalOrderStateException

data class Order(
    val id: Long? = null,
    val userId: Long,
    val items: List<OrderItem>,
    val totalAmount: Int,
    val status: OrderStatus,
    val paymentId: Long? = null,           // PAID 전이 시 채워짐
    val shippingAddress: String,
) {
    fun markPaid(paymentId: Long): Order {
        check(status == OrderStatus.PAYMENT_PENDING) {
            throw IllegalOrderStateException(status, OrderStatus.PAID)
        }
        return copy(status = OrderStatus.PAID, paymentId = paymentId)
    }

    fun startShipping(): Order {
        check(status == OrderStatus.PAID) {
            throw IllegalOrderStateException(status, OrderStatus.SHIPPING)
        }
        return copy(status = OrderStatus.SHIPPING)
    }

    fun markDelivered(): Order {
        check(status == OrderStatus.SHIPPING) {
            throw IllegalOrderStateException(status, OrderStatus.DELIVERED)
        }
        return copy(status = OrderStatus.DELIVERED)
    }

    fun cancel(): Order {
        check(status == OrderStatus.PAYMENT_PENDING || status == OrderStatus.PAID) {
            throw IllegalOrderStateException(status, OrderStatus.CANCELLED)
        }
        return copy(status = OrderStatus.CANCELLED)
    }

    companion object {
        fun create(userId: Long, items: List<OrderItem>, shippingAddress: String): Order {
            require(items.isNotEmpty()) { "주문 아이템은 최소 1개 이상이어야 한다" }
            require(shippingAddress.isNotBlank()) { "배송지는 필수" }
            val total = items.sumOf { it.subtotal }
            return Order(
                userId = userId, items = items, totalAmount = total,
                status = OrderStatus.PAYMENT_PENDING,    // 생성 즉시 결제 대기
                shippingAddress = shippingAddress,
            )
        }
    }
}
