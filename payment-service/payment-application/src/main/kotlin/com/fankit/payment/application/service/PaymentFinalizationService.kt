package com.fankit.payment.application.service

import com.fankit.payment.application.saga.SagaResult
import com.fankit.payment.domain.model.OutboxEvent
import com.fankit.payment.domain.model.Payment
import com.fankit.payment.domain.model.PaymentResult
import com.fankit.payment.domain.port.outbound.EventPayloadSerializer
import com.fankit.payment.domain.port.outbound.OutboxEventRepository
import com.fankit.payment.domain.port.outbound.PaymentRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

// Phase C — Saga 결과 반영 (트랜잭션)
//
// Payment status 업데이트 + Outbox 적재를 같은 트랜잭션에 묶어
// "결제 상태 변경됐는데 이벤트 발행 안 됨" 또는 그 역의 inconsistency 차단
//   → 이게 Transactional Outbox Pattern의 핵심
@Service
class PaymentFinalizationService(
    private val paymentRepository: PaymentRepository,
    private val outboxEventRepository: OutboxEventRepository,
    private val eventPayloadSerializer: EventPayloadSerializer,
) {
    @Transactional
    fun finalize(payment: Payment, sagaResult: SagaResult): PaymentResult =
        when (sagaResult) {
            is SagaResult.Success -> handleSuccess(payment, sagaResult)
            is SagaResult.Failure -> handleFailure(payment, sagaResult)
        }

    private fun handleSuccess(payment: Payment, result: SagaResult.Success): PaymentResult.Success {
        val approved = paymentRepository.save(payment.approve(result.pgTransactionId))
        outboxEventRepository.save(
            OutboxEvent(
                aggregateType = AGGREGATE_PAYMENT,
                aggregateId = approved.id.toString(),
                eventType = EVENT_PAYMENT_COMPLETED,
                payload = eventPayloadSerializer.serialize(
                    PaymentCompletedPayload(
                        paymentId = approved.id!!,
                        orderId = approved.orderId,
                        userId = approved.userId,
                        amount = approved.amount,
                        pgTransactionId = result.pgTransactionId,
                    )
                ),
            )
        )
        return PaymentResult.Success(approved.id!!, approved.amount, result.pgTransactionId)
    }

    private fun handleFailure(payment: Payment, result: SagaResult.Failure): PaymentResult.Failure {
        val failed = paymentRepository.save(payment.fail(result.reason))
        outboxEventRepository.save(
            OutboxEvent(
                aggregateType = AGGREGATE_PAYMENT,
                aggregateId = failed.id.toString(),
                eventType = EVENT_PAYMENT_FAILED,
                payload = eventPayloadSerializer.serialize(
                    PaymentFailedPayload(
                        paymentId = failed.id!!,
                        orderId = failed.orderId,
                        userId = failed.userId,
                        reason = result.reason,
                    )
                ),
            )
        )
        return PaymentResult.Failure(failed.id, result.reason)
    }

    companion object {
        private const val AGGREGATE_PAYMENT = "Payment"
        private const val EVENT_PAYMENT_COMPLETED = "payment.completed"
        private const val EVENT_PAYMENT_FAILED = "payment.failed"
    }
}

data class PaymentCompletedPayload(
    val paymentId: Long,
    val orderId: Long,
    val userId: Long,
    val amount: Int,
    val pgTransactionId: String,
)

data class PaymentFailedPayload(
    val paymentId: Long,
    val orderId: Long,
    val userId: Long,
    val reason: String,
)
