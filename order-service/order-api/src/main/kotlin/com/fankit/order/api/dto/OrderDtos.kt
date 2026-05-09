package com.fankit.order.api.dto

import com.fankit.order.domain.model.Order
import com.fankit.order.domain.model.OrderStatus
import com.fankit.order.domain.port.inbound.CreateOrderCommand
import com.fankit.order.domain.port.inbound.CreateOrderItemCommand
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

data class CreateOrderRequest(
    @field:Min(1) val userId: Long,
    @field:Valid @field:NotEmpty val items: List<OrderItemRequest>,
    @field:NotBlank val shippingAddress: String,
) {
    fun toCommand() = CreateOrderCommand(
        userId = userId,
        items = items.map { CreateOrderItemCommand(it.goodsId, it.goodsName, it.quantity, it.unitPrice) },
        shippingAddress = shippingAddress,
    )
}

data class OrderItemRequest(
    @field:NotBlank val goodsId: String,
    @field:NotBlank val goodsName: String,
    @field:Min(1) val quantity: Int,
    @field:Min(0) val unitPrice: Int,
)

data class ConfirmOrderRequest(
    @field:Min(1) val paymentId: Long,
)

data class OrderResponse(
    val id: Long,
    val userId: Long,
    val items: List<OrderItemResponse>,
    val totalAmount: Int,
    val status: OrderStatus,
    val paymentId: Long?,
    val shippingAddress: String,
) {
    companion object {
        fun from(o: Order) = OrderResponse(
            id = o.id!!, userId = o.userId,
            items = o.items.map { OrderItemResponse(it.goodsId, it.goodsName, it.quantity, it.unitPrice) },
            totalAmount = o.totalAmount, status = o.status, paymentId = o.paymentId,
            shippingAddress = o.shippingAddress,
        )
    }
}

data class OrderItemResponse(
    val goodsId: String,
    val goodsName: String,
    val quantity: Int,
    val unitPrice: Int,
)
