package com.fankit.order.infrastructure.persistence

import com.fankit.order.domain.model.OrderItem
import jakarta.persistence.Column
import jakarta.persistence.Embeddable

// @ElementCollection으로 order_items 테이블에 매핑됨 — 별도 entity 정의 불필요
// (OrderItem이 자체 정체성/생명주기 없이 Order에 종속된 value object이므로)
@Embeddable
data class OrderItemEmbeddable(
    @Column(name = "goods_id", nullable = false, length = 50)
    val goodsId: String = "",

    @Column(name = "goods_name", nullable = false, length = 200)
    val goodsName: String = "",

    @Column(nullable = false)
    val quantity: Int = 0,

    @Column(name = "unit_price", nullable = false)
    val unitPrice: Int = 0,
) {
    fun toDomain() = OrderItem(goodsId, goodsName, quantity, unitPrice)

    companion object {
        fun fromDomain(i: OrderItem) = OrderItemEmbeddable(i.goodsId, i.goodsName, i.quantity, i.unitPrice)
    }
}
