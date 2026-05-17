package com.fankit.payment.integration

import com.fankit.common.response.ApiResponse
import com.fankit.payment.api.dto.CreatePaymentRequest
import com.fankit.payment.api.dto.CreatePaymentResponse
import com.fankit.payment.api.dto.StockLineRequest
import com.fankit.payment.domain.model.OutboxStatus
import com.fankit.payment.domain.model.PaymentMethod
import com.fankit.payment.domain.model.PaymentStatus
import com.fankit.payment.domain.model.SagaStatus
import com.fankit.payment.domain.port.outbound.GoodsServiceClient
import com.fankit.payment.domain.port.outbound.OrderServiceClient
import com.fankit.payment.domain.port.outbound.PgApprovalRequest
import com.fankit.payment.domain.port.outbound.PgApprovalResponse
import com.fankit.payment.domain.port.outbound.PgCancelResponse
import com.fankit.payment.domain.port.outbound.PgClient
import com.fankit.payment.infrastructure.persistence.IdempotencyKeyJpaRepository
import com.fankit.payment.infrastructure.persistence.OutboxEventJpaRepository
import com.fankit.payment.infrastructure.persistence.PaymentJpaRepository
import com.fankit.payment.infrastructure.persistence.SagaLogJpaRepository
import com.ninjasquad.springmockk.MockkBean
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.verify
import io.mockk.verifyOrder
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import java.time.Duration
import java.util.UUID

/**
 * Payment Saga — 보상 트랜잭션 + Idempotency 통합 테스트
 *
 * 외부 클라이언트 3개(PgClient · OrderServiceClient · GoodsServiceClient)를
 * @MockkBean으로 교체해 각 step의 성공/실패를 결정적으로 주입한다.
 *
 * 시나리오
 *   ① Step 1 (PG 승인) 실패  → 보상할 step 없음 → 결제 실패만 기록
 *   ② Step 2 (주문 확정) 실패 → Step 1(PG) 보상 1회
 *   ③ Step 3 (재고 차감) 실패 → LIFO 보상: 주문 취소 → PG 취소 순서
 *   ④ 멱등성: 동일 X-Idempotency-Key 재요청 → 동일 paymentId, PG는 1회만 호출
 */
class PaymentSagaCompensationIntegrationTest @Autowired constructor(
    private val paymentRepository: PaymentJpaRepository,
    private val sagaLogRepository: SagaLogJpaRepository,
    private val outboxRepository: OutboxEventJpaRepository,
    private val idempotencyRepository: IdempotencyKeyJpaRepository,
) : PaymentIntegrationTestSupport() {

    @MockkBean
    private lateinit var pgClient: PgClient

    @MockkBean
    private lateinit var orderServiceClient: OrderServiceClient

    @MockkBean
    private lateinit var goodsServiceClient: GoodsServiceClient

    @BeforeEach
    fun resetState() {
        outboxRepository.deleteAll()
        sagaLogRepository.deleteAll()
        idempotencyRepository.deleteAll()
        paymentRepository.deleteAll()
    }

    @AfterEach
    fun cleanup() {
        outboxRepository.deleteAll()
        sagaLogRepository.deleteAll()
        idempotencyRepository.deleteAll()
        paymentRepository.deleteAll()
    }

    // ════════════════════════════════════════════════════════════════
    //  ① Step 1 (PG 승인) 실패 — 보상할 step 없음
    // ════════════════════════════════════════════════════════════════
    @Test
    fun `PG 승인 실패 시 후속 step 호출 없이 결제 실패 기록`() {
        // given — PG가 예외 throw
        every { pgClient.approve(any()) } throws RuntimeException("PG 게이트웨이 timeout")

        // when
        val response = postPayment(idempotencyKey = UUID.randomUUID().toString(), orderId = 2001L)

        // then — HTTP 201이지만 응답 본문은 failure
        response.statusCode shouldBe HttpStatus.CREATED
        val data = response.body!!.data.shouldNotBeNull()
        data.success shouldBe false
        data.failureReason shouldNotBe null

        // 후속 step은 절대 호출되지 않아야 함
        verify(exactly = 0) { orderServiceClient.confirm(any()) }
        verify(exactly = 0) { goodsServiceClient.decreaseStock(any()) }
        // PG 자체가 실패라 보상할 게 없음 → cancel 호출 X
        verify(exactly = 0) { pgClient.cancel(any()) }

        // DB — Payment FAILED, SagaLog COMPENSATED (보상할 게 없으나 흐름상 종결)
        val payment = paymentRepository.findAll().single { it.orderId == 2001L }
        payment.status shouldBe PaymentStatus.FAILED
        val sagaLog = sagaLogRepository.findAll().filter { it.paymentId == payment.id }
            .maxByOrNull { it.id ?: 0 }.shouldNotBeNull()
        sagaLog.status shouldBe SagaStatus.COMPENSATED
        sagaLog.failureReason shouldNotBe null

        // Outbox — payment.failed 이벤트 PUBLISHED
        awaitOutboxPublished(payment.id!!, expectedEventType = "payment.failed")
    }

    // ════════════════════════════════════════════════════════════════
    //  ② Step 2 (주문 확정) 실패 → Step 1(PG) 보상
    // ════════════════════════════════════════════════════════════════
    @Test
    fun `주문 확정 실패 시 PG 취소 보상 1회 실행`() {
        // given — PG는 성공, Order confirm은 실패
        every { pgClient.approve(any()) } returns PgApprovalResponse(
            pgTransactionId = "kakao_test_step2", approvedAmount = 5_000,
        )
        every { pgClient.cancel("kakao_test_step2") } returns PgCancelResponse(
            pgTransactionId = "kakao_test_step2", cancelledAmount = 5_000,
        )
        every { orderServiceClient.confirm(any()) } throws RuntimeException("Order Service 5xx")

        // when
        val response = postPayment(idempotencyKey = UUID.randomUUID().toString(), orderId = 2002L)

        // then
        response.statusCode shouldBe HttpStatus.CREATED
        response.body!!.data!!.success shouldBe false

        verify(exactly = 1) { pgClient.approve(any()) }
        verify(exactly = 1) { orderServiceClient.confirm(2002L) }
        verify(exactly = 1) { pgClient.cancel("kakao_test_step2") }   // ✅ 보상 1회
        // Step 3는 도달하지 못함
        verify(exactly = 0) { goodsServiceClient.decreaseStock(any()) }
        verify(exactly = 0) { orderServiceClient.cancel(any()) }

        val payment = paymentRepository.findAll().single { it.orderId == 2002L }
        payment.status shouldBe PaymentStatus.FAILED
        val sagaLog = sagaLogRepository.findAll().filter { it.paymentId == payment.id }
            .maxByOrNull { it.id ?: 0 }!!
        sagaLog.status shouldBe SagaStatus.COMPENSATED
    }

    // ════════════════════════════════════════════════════════════════
    //  ③ Step 3 (재고 차감) 실패 → LIFO 보상 (Order cancel → PG cancel)
    // ════════════════════════════════════════════════════════════════
    @Test
    fun `재고 차감 실패 시 LIFO 순서로 주문취소→PG취소 보상`() {
        // given
        every { pgClient.approve(any()) } returns PgApprovalResponse(
            pgTransactionId = "kakao_test_step3", approvedAmount = 5_000,
        )
        every { pgClient.cancel("kakao_test_step3") } returns PgCancelResponse(
            pgTransactionId = "kakao_test_step3", cancelledAmount = 5_000,
        )
        every { orderServiceClient.confirm(any()) } returns Unit
        every { orderServiceClient.cancel(any()) } returns Unit
        every { goodsServiceClient.decreaseStock(any()) } throws RuntimeException("재고 부족")

        // when
        val response = postPayment(idempotencyKey = UUID.randomUUID().toString(), orderId = 2003L)

        // then — 보상이 LIFO 순서로 실행
        response.statusCode shouldBe HttpStatus.CREATED
        response.body!!.data!!.success shouldBe false

        verifyOrder {
            pgClient.approve(any())                  // step 1
            orderServiceClient.confirm(2003L)        // step 2
            goodsServiceClient.decreaseStock(any())  // step 3 (실패)
            orderServiceClient.cancel(2003L)         // 보상 1: 주문 취소
            pgClient.cancel("kakao_test_step3")      // 보상 2: PG 취소 (LIFO 마지막)
        }

        val payment = paymentRepository.findAll().single { it.orderId == 2003L }
        payment.status shouldBe PaymentStatus.FAILED
        val sagaLog = sagaLogRepository.findAll().filter { it.paymentId == payment.id }
            .maxByOrNull { it.id ?: 0 }!!
        sagaLog.status shouldBe SagaStatus.COMPENSATED
    }

    // ════════════════════════════════════════════════════════════════
    //  ④ 멱등성: 같은 X-Idempotency-Key 재요청 → PG 1회만 호출
    // ════════════════════════════════════════════════════════════════
    @Test
    fun `동일 Idempotency Key 재요청 시 동일 paymentId 반환 + PG는 1회만 호출`() {
        // given — happy path 흐름
        every { pgClient.approve(any()) } returns PgApprovalResponse(
            pgTransactionId = "kakao_test_idem", approvedAmount = 5_000,
        )
        every { orderServiceClient.confirm(any()) } returns Unit
        every { goodsServiceClient.decreaseStock(any()) } returns Unit

        val key = UUID.randomUUID().toString()

        // when — 같은 키로 두 번 POST
        val first = postPayment(idempotencyKey = key, orderId = 2004L)
        val second = postPayment(idempotencyKey = key, orderId = 2004L)

        // then — 두 응답 모두 success, paymentId 동일
        first.body!!.data!!.success shouldBe true
        second.body!!.data!!.success shouldBe true
        val firstId = first.body!!.data!!.paymentId
        val secondId = second.body!!.data!!.paymentId
        firstId shouldNotBe null
        secondId shouldBe firstId

        // 핵심 검증 — Saga 전 단계가 두 번째 요청에서는 절대 호출되지 않아야 함
        verify(exactly = 1) { pgClient.approve(any()) }
        verify(exactly = 1) { orderServiceClient.confirm(any()) }
        verify(exactly = 1) { goodsServiceClient.decreaseStock(any()) }

        // Payment row · idempotencyKey 레코드는 각각 정확히 1건
        paymentRepository.findAll().filter { it.orderId == 2004L } shouldHaveSize 1
        idempotencyRepository.findByIdempotencyKey(key).shouldNotBeNull()
    }

    // ────────────────────────────────────────────────────────────────
    //  helpers
    // ────────────────────────────────────────────────────────────────
    private fun postPayment(idempotencyKey: String, orderId: Long) =
        restTemplate.exchange(
            url("/api/v1/payments"),
            HttpMethod.POST,
            HttpEntity(
                CreatePaymentRequest(
                    orderId = orderId, userId = 42L, amount = 5_000,
                    method = PaymentMethod.KAKAO_PAY,
                    items = listOf(StockLineRequest(goodsId = "GOODS-1", quantity = 1)),
                ),
                HttpHeaders().apply {
                    contentType = MediaType.APPLICATION_JSON
                    set("X-Idempotency-Key", idempotencyKey)
                },
            ),
            object : ParameterizedTypeReference<ApiResponse<CreatePaymentResponse>>() {},
        )

    private fun awaitOutboxPublished(paymentId: Long, expectedEventType: String) {
        await().atMost(Duration.ofSeconds(10)).pollInterval(Duration.ofMillis(300)).untilAsserted {
            val events = outboxRepository.findAll().filter { it.aggregateId == paymentId.toString() }
            events shouldHaveSize 1
            val event = events.first()
            event.eventType shouldBe expectedEventType
            event.status shouldBe OutboxStatus.PUBLISHED
        }
    }
}
