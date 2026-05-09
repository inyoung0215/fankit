package com.fankit.payment.domain

import com.fankit.payment.domain.exception.IllegalPaymentStateException
import com.fankit.payment.domain.model.Payment
import com.fankit.payment.domain.model.PaymentMethod
import com.fankit.payment.domain.model.PaymentStatus
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class PaymentTest {

    private fun pending() = Payment.create(
        orderId = 1L, userId = 10L, amount = 5000,
        method = PaymentMethod.KAKAO_PAY, idempotencyKey = "key-1",
    )

    @Test
    fun `create는 PENDING 상태로 시작한다`() {
        pending().status shouldBe PaymentStatus.PENDING
    }

    @Test
    fun `금액이 100원 미만이면 생성 실패`() {
        shouldThrow<IllegalArgumentException> {
            Payment.create(1L, 10L, 50, PaymentMethod.KAKAO_PAY, "k")
        }
    }

    @Test
    fun `PENDING approve 시 APPROVED + pgTransactionId 세팅`() {
        val approved = pending().approve("kakao_tx_123")
        approved.status shouldBe PaymentStatus.APPROVED
        approved.pgTransactionId shouldBe "kakao_tx_123"
    }

    @Test
    fun `APPROVED는 다시 approve 불가`() {
        val approved = pending().approve("tx")
        shouldThrow<IllegalPaymentStateException> { approved.approve("tx2") }
    }

    @Test
    fun `APPROVED만 refund 가능 - PENDING에서 refund 불가`() {
        shouldThrow<IllegalPaymentStateException> { pending().refund() }
        pending().approve("tx").refund().status shouldBe PaymentStatus.REFUNDED
    }

    @Test
    fun `cancel은 PENDING에서만 가능`() {
        pending().cancel().status shouldBe PaymentStatus.CANCELLED
        shouldThrow<IllegalPaymentStateException> { pending().approve("tx").cancel() }
    }
}
