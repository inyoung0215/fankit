package com.fankit.payment.integration

import com.fankit.payment.api.dto.CreatePaymentRequest
import com.fankit.payment.api.dto.CreatePaymentResponse
import com.fankit.payment.api.dto.StockLineRequest
import com.fankit.payment.domain.model.OutboxStatus
import com.fankit.payment.domain.model.PaymentMethod
import com.fankit.payment.domain.model.PaymentStatus
import com.fankit.payment.domain.model.SagaStatus
import com.fankit.payment.domain.model.SagaStep
import com.fankit.payment.infrastructure.persistence.IdempotencyKeyJpaRepository
import com.fankit.payment.infrastructure.persistence.OutboxEventJpaRepository
import com.fankit.payment.infrastructure.persistence.PaymentJpaRepository
import com.fankit.payment.infrastructure.persistence.SagaLogJpaRepository
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.core.ParameterizedTypeReference
import com.fankit.common.response.ApiResponse
import java.time.Duration
import java.util.UUID

/**
 * Payment Saga — Happy Path 통합 테스트
 *
 * 시나리오: PG / Order / Goods 호출이 모두 성공하는 케이스
 *
 * 검증 포인트
 *   1. HTTP 201 + success=true 응답, paymentId & pgTransactionId 반환
 *   2. payments 테이블: status=APPROVED, pg_transaction_id 저장
 *   3. saga_logs: status=COMPLETED, current_step=DONE
 *   4. idempotency_keys: payment row와 1:1 매핑 저장
 *   5. outbox_events: payment.completed 이벤트가 발행(PUBLISHED) — 비동기, Awaitility로 대기
 */
class PaymentSagaHappyPathIntegrationTest @Autowired constructor(
    private val paymentRepository: PaymentJpaRepository,
    private val sagaLogRepository: SagaLogJpaRepository,
    private val outboxRepository: OutboxEventJpaRepository,
    private val idempotencyRepository: IdempotencyKeyJpaRepository,
) : PaymentIntegrationTestSupport() {

    @AfterEach
    fun cleanup() {
        // Outbox relay가 다음 테스트에 영향 주지 않도록 정리
        outboxRepository.deleteAll()
        sagaLogRepository.deleteAll()
        idempotencyRepository.deleteAll()
        paymentRepository.deleteAll()
    }

    @Test
    fun `결제 요청 → Saga 전 단계 성공 → DB 상태 + Outbox 발행 모두 일관됨`() {
        // given
        val idempotencyKey = UUID.randomUUID().toString()
        val orderId = 1001L
        val userId = 42L
        // amount < 10_000 → MockKakaoPgClient는 100% 성공 (결정적 happy path)
        val amount = 5_000
        val request = CreatePaymentRequest(
            orderId = orderId,
            userId = userId,
            amount = amount,
            method = PaymentMethod.KAKAO_PAY,
            items = listOf(StockLineRequest(goodsId = "GOODS-1", quantity = 1)),
        )

        // when — POST /api/v1/payments (X-Idempotency-Key 헤더)
        val response = restTemplate.exchange(
            url("/api/v1/payments"),
            HttpMethod.POST,
            HttpEntity(request, HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
                set("X-Idempotency-Key", idempotencyKey)
            }),
            object : ParameterizedTypeReference<ApiResponse<CreatePaymentResponse>>() {},
        )

        // then — HTTP 응답 확인
        response.statusCode shouldBe HttpStatus.CREATED
        val body = response.body.shouldNotBeNull()
        body.success shouldBe true
        val data = body.data.shouldNotBeNull()
        data.success shouldBe true
        data.paymentId.shouldNotBeNull()
        data.amount shouldBe amount
        data.pgTransactionId.shouldNotBeNull() shouldStartWith "kakao_"
        data.failureReason shouldBe null

        // then — Payment row APPROVED
        val payment = paymentRepository.findById(data.paymentId!!).orElseThrow()
        payment.status shouldBe PaymentStatus.APPROVED
        payment.pgTransactionId shouldBe data.pgTransactionId
        payment.failureReason shouldBe null

        // then — SagaLog COMPLETED + DONE
        val sagaLogs = sagaLogRepository.findAll().filter { it.paymentId == payment.id }
        // saga는 진행 중 여러 row를 적재할 수 있음 (STARTED → ORDER_CONFIRM → STOCK_DECREASE → DONE)
        val lastSaga = sagaLogs.maxByOrNull { it.id ?: 0 }.shouldNotBeNull()
        lastSaga.status shouldBe SagaStatus.COMPLETED
        lastSaga.currentStep shouldBe SagaStep.DONE
        lastSaga.failureReason shouldBe null

        // then — IdempotencyKey 저장 (X-Idempotency-Key → paymentId 매핑)
        val idemRecord = idempotencyRepository.findByIdempotencyKey(idempotencyKey).shouldNotBeNull()
        idemRecord.paymentId shouldBe payment.id

        // then — Outbox payment.completed 이벤트가 비동기로 PUBLISHED 처리됨
        // OutboxRelayScheduler가 1초 주기로 발행, 최대 10초 대기
        await().atMost(Duration.ofSeconds(10)).pollInterval(Duration.ofMillis(300)).untilAsserted {
            val events = outboxRepository.findAll()
                .filter { it.aggregateId == payment.id.toString() }
            events shouldHaveSize 1
            val event = events.first()
            event.eventType shouldBe "payment.completed"
            event.aggregateType shouldBe "Payment"
            event.status shouldBe OutboxStatus.PUBLISHED
            event.publishedAt.shouldNotBeNull()
        }
    }
}
