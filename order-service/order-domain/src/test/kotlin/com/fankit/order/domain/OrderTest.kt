package com.fankit.order.domain

import com.fankit.order.domain.exception.IllegalOrderStateException
import com.fankit.order.domain.model.Order
import com.fankit.order.domain.model.OrderItem
import com.fankit.order.domain.model.OrderStatus
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class OrderTest {

    private fun newOrder() = Order.create(
        userId = 1L,
        items = listOf(
            OrderItem("g1", "민호 키링", quantity = 2, unitPrice = 5000),
            OrderItem("g2", "포토카드 세트", quantity = 1, unitPrice = 3000),
        ),
        shippingAddress = "서울시 강남구",
    )

    @Test
    fun `create는 PAYMENT_PENDING 상태로 시작 + 합계 금액 자동 계산`() {
        val order = newOrder()
        order.status shouldBe OrderStatus.PAYMENT_PENDING
        order.totalAmount shouldBe 13_000   // 5000*2 + 3000
    }

    @Test
    fun `아이템이 비어있으면 생성 실패`() {
        shouldThrow<IllegalArgumentException> {
            Order.create(1L, emptyList(), "주소")
        }
    }

    @Test
    fun `markPaid는 PAYMENT_PENDING 에서만 가능`() {
        val paid = newOrder().markPaid(99L)
        paid.status shouldBe OrderStatus.PAID
        paid.paymentId shouldBe 99L

        shouldThrow<IllegalOrderStateException> { paid.markPaid(100L) }
    }

    @Test
    fun `cancel은 PAYMENT_PENDING 또는 PAID 에서만 가능`() {
        newOrder().cancel().status shouldBe OrderStatus.CANCELLED
        newOrder().markPaid(1L).cancel().status shouldBe OrderStatus.CANCELLED

        val shipping = newOrder().markPaid(1L).startShipping()
        shouldThrow<IllegalOrderStateException> { shipping.cancel() }
    }

    @Test
    fun `정상 배송 완료 시퀀스`() {
        val delivered = newOrder().markPaid(1L).startShipping().markDelivered()
        delivered.status shouldBe OrderStatus.DELIVERED
    }
}
