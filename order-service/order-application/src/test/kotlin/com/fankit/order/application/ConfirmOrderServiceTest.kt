package com.fankit.order.application

import com.fankit.order.domain.exception.OrderNotFoundException
import com.fankit.order.domain.model.Order
import com.fankit.order.domain.model.OrderItem
import com.fankit.order.domain.model.OrderStatus
import com.fankit.order.domain.port.outbound.OrderRepository
import io.kotest.assertions.throwables.shouldThrow
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class ConfirmOrderServiceTest {

    private val repository: OrderRepository = mockk()
    private val service = ConfirmOrderService(repository)

    private fun pendingOrder() = Order(
        id = 1L, userId = 10L,
        items = listOf(OrderItem("g1", "굿즈", 1, 1000)),
        totalAmount = 1000, status = OrderStatus.PAYMENT_PENDING,
        shippingAddress = "주소",
    )

    @Test
    fun `존재하지 않는 주문이면 OrderNotFoundException`() {
        every { repository.findById(99L) } returns null
        shouldThrow<OrderNotFoundException> { service.confirm(99L, 1L) }
    }

    @Test
    fun `정상 confirm — PAYMENT_PENDING 에서 PAID 전이 + paymentId 저장`() {
        every { repository.findById(1L) } returns pendingOrder()
        every { repository.save(any()) } answers { firstArg() }

        service.confirm(1L, 99L)

        verify {
            repository.save(match {
                it.status == OrderStatus.PAID && it.paymentId == 99L
            })
        }
    }

    @Test
    fun `이미 PAID인 주문은 멱등 skip — Kafka 재전송 안전`() {
        val paid = pendingOrder().markPaid(99L)
        every { repository.findById(1L) } returns paid

        service.confirm(1L, 99L)

        verify(exactly = 0) { repository.save(any()) }
    }
}
