package com.fankit.order.application

import com.fankit.order.domain.port.inbound.CancelOrderUseCase
import com.fankit.order.domain.port.inbound.ConfirmOrderUseCase
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class PaymentEventHandlerTest {

    private val confirm: ConfirmOrderUseCase = mockk()
    private val cancel: CancelOrderUseCase = mockk()
    private val handler = PaymentEventHandler(confirm, cancel)

    @Test
    fun `payment_completed 이벤트는 ConfirmOrderUseCase로 라우팅`() {
        every { confirm.confirm(1L, 99L) } just Runs

        handler.onPaymentCompleted(orderId = 1L, paymentId = 99L)

        verify { confirm.confirm(1L, 99L) }
        verify(exactly = 0) { cancel.cancel(any()) }
    }

    @Test
    fun `payment_failed 이벤트는 CancelOrderUseCase로 라우팅`() {
        every { cancel.cancel(1L) } just Runs

        handler.onPaymentFailed(orderId = 1L)

        verify { cancel.cancel(1L) }
        verify(exactly = 0) { confirm.confirm(any(), any()) }
    }
}
