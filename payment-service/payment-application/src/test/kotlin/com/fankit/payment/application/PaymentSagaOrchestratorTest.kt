package com.fankit.payment.application

import com.fankit.payment.application.saga.PaymentSagaOrchestrator
import com.fankit.payment.application.saga.SagaResult
import com.fankit.payment.domain.model.Payment
import com.fankit.payment.domain.model.PaymentMethod
import com.fankit.payment.domain.model.PaymentStatus
import com.fankit.payment.domain.model.SagaLog
import com.fankit.payment.domain.model.SagaStatus
import com.fankit.payment.domain.model.SagaStep
import com.fankit.payment.domain.port.inbound.StockLineCommand
import com.fankit.payment.domain.port.outbound.GoodsServiceClient
import com.fankit.payment.domain.port.outbound.OrderServiceClient
import com.fankit.payment.domain.port.outbound.PgApprovalRequest
import com.fankit.payment.domain.port.outbound.PgApprovalResponse
import com.fankit.payment.domain.port.outbound.PgCancelResponse
import com.fankit.payment.domain.port.outbound.PgClient
import com.fankit.payment.domain.port.outbound.SagaLogRepository
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test

class PaymentSagaOrchestratorTest {

    private val pgClient: PgClient = mockk()
    private val orderClient: OrderServiceClient = mockk()
    private val goodsClient: GoodsServiceClient = mockk()
    private val sagaLogRepository: SagaLogRepository = mockk()

    private val orchestrator = PaymentSagaOrchestrator(
        pgClient, orderClient, goodsClient, sagaLogRepository,
    )

    private val payment = Payment(
        id = 100L, orderId = 1L, userId = 10L, amount = 5000,
        method = PaymentMethod.KAKAO_PAY, status = PaymentStatus.PENDING,
        idempotencyKey = "key-1",
    )

    private val items = listOf(StockLineCommand("g1", 2))

    private fun stubSagaLogSaveEcho() {
        every { sagaLogRepository.save(any()) } answers { firstArg<SagaLog>() }
    }

    @Test
    fun `모든 step 성공 시 Success 반환 + saga COMPLETED 마킹`() {
        stubSagaLogSaveEcho()
        every { pgClient.approve(any()) } returns PgApprovalResponse("tx_kakao", 5000)
        every { orderClient.confirm(1L) } just Runs
        every { goodsClient.decreaseStock(any()) } just Runs

        val result = orchestrator.execute(payment, items)

        result.shouldBeInstanceOf<SagaResult.Success>().pgTransactionId shouldBe "tx_kakao"
        // 마지막 save 호출은 status=COMPLETED, step=DONE
        verify {
            sagaLogRepository.save(match { it.status == SagaStatus.COMPLETED && it.currentStep == SagaStep.DONE })
        }
    }

    @Test
    fun `step1 PG 승인 실패 시 보상 없이 Failure - 이전 step 없음`() {
        stubSagaLogSaveEcho()
        every { pgClient.approve(any()) } throws RuntimeException("PG down")

        val result = orchestrator.execute(payment, items)

        result.shouldBeInstanceOf<SagaResult.Failure>()
        verify(exactly = 0) { orderClient.confirm(any()) }
        verify(exactly = 0) { goodsClient.decreaseStock(any()) }
        verify(exactly = 0) { pgClient.cancel(any()) }
    }

    @Test
    fun `step2 주문확정 실패 시 PG 취소 보상`() {
        stubSagaLogSaveEcho()
        every { pgClient.approve(any()) } returns PgApprovalResponse("tx_kakao", 5000)
        every { orderClient.confirm(1L) } throws RuntimeException("order down")
        every { pgClient.cancel("tx_kakao") } returns PgCancelResponse("tx_kakao", 5000)

        val result = orchestrator.execute(payment, items)

        result.shouldBeInstanceOf<SagaResult.Failure>()
        verify { pgClient.cancel("tx_kakao") }
        verify { sagaLogRepository.save(match { it.status == SagaStatus.COMPENSATED }) }
    }

    @Test
    fun `step3 재고 차감 실패 시 LIFO 보상 - 주문 취소 + PG 취소`() {
        stubSagaLogSaveEcho()
        every { pgClient.approve(any()) } returns PgApprovalResponse("tx_kakao", 5000)
        every { orderClient.confirm(1L) } just Runs
        every { goodsClient.decreaseStock(any()) } throws RuntimeException("stock fail")
        every { orderClient.cancel(1L) } just Runs
        every { pgClient.cancel("tx_kakao") } returns PgCancelResponse("tx_kakao", 5000)

        val result = orchestrator.execute(payment, items)

        result.shouldBeInstanceOf<SagaResult.Failure>()
        verify { orderClient.cancel(1L) }
        verify { pgClient.cancel("tx_kakao") }
    }

    @Test
    fun `보상도 실패하면 COMPENSATION_FAILED 마킹 - 운영 개입 신호`() {
        val capturedLogs = mutableListOf<SagaLog>()
        every { sagaLogRepository.save(capture(capturedLogs)) } answers { firstArg() }
        every { pgClient.approve(any()) } returns PgApprovalResponse("tx_kakao", 5000)
        every { orderClient.confirm(1L) } just Runs
        every { goodsClient.decreaseStock(any()) } throws RuntimeException("stock fail")
        every { orderClient.cancel(1L) } throws RuntimeException("order cancel fail")
        every { pgClient.cancel("tx_kakao") } throws RuntimeException("pg cancel fail")

        orchestrator.execute(payment, items)

        capturedLogs.last().status shouldBe SagaStatus.COMPENSATION_FAILED
    }
}
