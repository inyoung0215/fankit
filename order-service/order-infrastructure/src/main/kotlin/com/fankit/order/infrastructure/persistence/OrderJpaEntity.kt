package com.fankit.order.infrastructure.persistence

import com.fankit.common.entity.BaseEntity
import com.fankit.order.domain.model.Order
import com.fankit.order.domain.model.OrderStatus
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table

@Entity
@Table(
    name = "orders",
    indexes = [
        Index(name = "idx_orders_user_id", columnList = "user_id"),
        Index(name = "idx_orders_status", columnList = "status"),
    ],
)
class OrderJpaEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "user_id", nullable = false)
    var userId: Long,

    @Column(name = "total_amount", nullable = false)
    var totalAmount: Int,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var status: OrderStatus,

    @Column(name = "payment_id")
    var paymentId: Long? = null,

    @Column(name = "shipping_address", nullable = false, length = 500)
    var shippingAddress: String,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "order_items",
        joinColumns = [JoinColumn(name = "order_id")],
    )
    var items: MutableList<OrderItemEmbeddable> = mutableListOf(),
) : BaseEntity() {

    fun toDomain(): Order = Order(
        id = id, userId = userId, items = items.map { it.toDomain() },
        totalAmount = totalAmount, status = status, paymentId = paymentId,
        shippingAddress = shippingAddress,
    )

    companion object {
        fun fromDomain(o: Order): OrderJpaEntity = OrderJpaEntity(
            id = o.id, userId = o.userId, totalAmount = o.totalAmount,
            status = o.status, paymentId = o.paymentId, shippingAddress = o.shippingAddress,
            items = o.items.map { OrderItemEmbeddable.fromDomain(it) }.toMutableList(),
        )
    }
}
