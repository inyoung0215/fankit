package com.fankit.payment.application

import com.fankit.payment.application.saga.PaymentSagaOrchestrator
import com.fankit.payment.application.saga.SagaResult
import com.fankit.payment.application.service.CreatePaymentService
import com.fankit.payment.application.service.PaymentFinalizationService
import com.fankit.payment.application.service.PaymentPreparationService
import com.fankit.payment.application.service.PreparedPayment
import com.fankit.payment.domain.model.Payment
import com.fankit.payment.domain.model.PaymentMethod
import com.fankit.payment.domain.model.PaymentResult
import com.fankit.payment.domain.model.PaymentStatus
import com.fankit.payment.domain.port.inbound.CreatePaymentCommand
import com.fankit.payment.domain.port.inbound.StockLineCommand
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class CreatePaymentServiceTest {

    private val preparation: PaymentPreparationService = mockk()
    private val saga: PaymentSagaOrchestrator = mockk()
    private val finalization: PaymentFinalizationService = mockk()
    private val service = CreatePaymentService(preparation, saga, finalization)

    private val command = CreatePaymentCommand(
        orderId = 1L, userId = 10L, amount = 5000, method = PaymentMethod.KAKAO_PAY,
        idempotencyKey = "key-1", items = listOf(StockLineCommand("g1", 1)),
    )

    private fun pendingPayment(pgTx: String? = null) = Payment(
        id = 100L, orderId = 1L, userId = 10L, amount = 5000,
        method = PaymentMethod.KAKAO_PAY, status = PaymentStatus.PENDING,
        idempotencyKey = "key-1", pgTransactionId = pgTx,
    )

    @Test
    fun `Idempotency Key 재사용 시 Saga 호출 없이 이전 결과 반환`() {
        val previous = pendingPayment(pgTx = "tx_old")
        every { preparation.prepare(command) } returns PreparedPayment(previous, alreadyProcessed = true)

        val result = service.create(command)

        result.shouldBeInstanceOf<PaymentResult.Success>()
        (result as PaymentResult.Success).pgTransactionId shouldBe "tx_old"
        verify(exactly = 0) { saga.execute(any(), any()) }
        verify(exactly = 0) { finalization.finalize(any(), any()) }
    }

    @Test
    fun `신규 요청은 Phase A → Saga → Phase C 순으로 실행`() {
        val prepared = pendingPayment()
        every { preparation.prepare(command) } returns PreparedPayment(prepared, alreadyProcessed = false)
        every { saga.execute(prepared, command.items) } returns SagaResult.Success("tx_kakao")
        every { finalization.finalize(prepared, any()) } returns
            PaymentResult.Success(100L, 5000, "tx_kakao")

        val result = service.create(command)

        result.shouldBeInstanceOf<PaymentResult.Success>()
        verify { preparation.prepare(command) }
        verify { saga.execute(prepared, command.items) }
        verify { finalization.finalize(prepared, match { it is SagaResult.Success }) }
    }

    @Test
    fun `Saga 실패 시 finalization이 PaymentResult Failure 반환`() {
        val prepared = pendingPayment()
        every { preparation.prepare(command) } returns PreparedPayment(prepared, alreadyProcessed = false)
        every { saga.execute(any(), any()) } returns SagaResult.Failure("PG down")
        every { finalization.finalize(prepared, any()) } returns PaymentResult.Failure(100L, "PG down")

        val result = service.create(command)

        result.shouldBeInstanceOf<PaymentResult.Failure>()
    }
}
