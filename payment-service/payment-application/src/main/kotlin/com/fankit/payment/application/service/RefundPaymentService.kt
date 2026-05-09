package com.fankit.payment.application.service

import com.fankit.payment.domain.exception.PaymentNotFoundException
import com.fankit.payment.domain.model.OutboxEvent
import com.fankit.payment.domain.model.Payment
import com.fankit.payment.domain.port.inbound.RefundPaymentUseCase
import com.fankit.payment.domain.port.outbound.EventPayloadSerializer
import com.fankit.payment.domain.port.outbound.OutboxEventRepository
import com.fankit.payment.domain.port.outbound.PaymentRepository
import com.fankit.payment.domain.port.outbound.PgClient
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

// 환불 — 단순화: PG 취소 호출 + 상태 전이 + 이벤트 적재
// (실제 운영에선 환불도 Saga로 묶어 정산 시스템과의 정합성 보장 필요)
@Service
class RefundPaymentService(
    private val paymentRepository: PaymentRepository,
    private val pgClient: PgClient,
    private val outboxEventRepository: OutboxEventRepository,
    private val eventPayloadSerializer: EventPayloadSerializer,
) : RefundPaymentUseCase {

    @Transactional
    override fun refund(paymentId: Long): Payment {
        val payment = paymentRepository.findById(paymentId) ?: throw PaymentNotFoundException(paymentId)

        // PG 취소 호출 — 트랜잭션 안에 있음 (단순화). 실전에선 Saga로 분리
        pgClient.cancel(payment.pgTransactionId ?: error("환불할 PG 거래 ID가 없다"))

        val refunded = paymentRepository.save(payment.refund())
        outboxEventRepository.save(
            OutboxEvent(
                aggregateType = "Payment",
                aggregateId = refunded.id.toString(),
                eventType = "payment.refunded",
                payload = eventPayloadSerializer.serialize(
                    RefundPayload(refunded.id!!, refunded.orderId, refunded.amount)
                ),
            )
        )
        return refunded
    }

    private data class RefundPayload(val paymentId: Long, val orderId: Long, val amount: Int)
}
