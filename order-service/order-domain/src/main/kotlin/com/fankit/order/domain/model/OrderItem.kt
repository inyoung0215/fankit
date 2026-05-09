package com.fankit.order.domain.model

// 주문 시점의 가격·이름 스냅샷 — Goods의 현재 값과 분리
// 이유: 굿즈 가격이 사후에 변경되어도 주문 금액·표시 이름은 보존되어야 함
data class OrderItem(
    val goodsId: String,
    val goodsName: String,
    val quantity: Int,
    val unitPrice: Int,
) {
    init {
        require(goodsId.isNotBlank()) { "goodsId는 비어있을 수 없다" }
        require(goodsName.isNotBlank()) { "goodsName은 비어있을 수 없다" }
        require(quantity in 1..1000) { "수량은 1~1000 사이여야 한다" }
        require(unitPrice >= 0) { "단가는 0 이상이어야 한다" }
    }

    val subtotal: Int get() = unitPrice * quantity
}
