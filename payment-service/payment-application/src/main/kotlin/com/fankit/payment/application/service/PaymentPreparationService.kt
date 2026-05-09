package com.fankit.payment.application.service

import com.fankit.payment.domain.exception.OrderAlreadyPaidException
import com.fankit.payment.domain.model.IdempotencyRecord
import com.fankit.payment.domain.model.Payment
import com.fankit.payment.domain.model.PaymentStatus
import com.fankit.payment.domain.port.inbound.CreatePaymentCommand
import com.fankit.payment.domain.port.outbound.IdempotencyCache
import com.fankit.payment.domain.port.outbound.IdempotencyKeyRepository
import com.fankit.payment.domain.port.outbound.PaymentRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

// Phase A — 결제 행 생성 (트랜잭션)
//
// 트랜잭션 내에서 빠르게 끝나야 할 일들:
//   1) Idempotency Key 이중 체크 (Redis → DB unique)
//   2) 동일 orderId 중복 결제 차단 (비관적 락)
//   3) Payment row INSERT (PENDING)
//
// Saga의 외부 HTTP 호출은 절대 이 트랜잭션 안에 두지 않는다 — 커넥션 점유 + timeout
@Service
class PaymentPreparationService(
    private val paymentRepository: PaymentRepository,
    private val idempotencyKeyRepository: IdempotencyKeyRepository,
    private val idempotencyCache: IdempotencyCache,
) {
    @Transactional
    fun prepare(command: CreatePaymentCommand): PreparedPayment {
        // ① Redis 1차 빠른 체크
        idempotencyCache.get(command.idempotencyKey)?.let { existingId ->
            paymentRepository.findById(existingId)?.let {
                return PreparedPayment(it, alreadyProcessed = true)
            }
        }

        // ② 동일 주문 중복 결제 차단 (SELECT FOR UPDATE)
        // 첫 트랜잭션이 lock 잡고 commit 후, 두 번째 요청은 이미 APPROVED 보고 거절
        paymentRepository.findByOrderIdForUpdate(command.orderId)?.let {
            if (it.status == PaymentStatus.APPROVED) {
                throw OrderAlreadyPaidException(command.orderId)
            }
        }

        // ③ Payment 생성
        val payment = Payment.create(
            orderId = command.orderId, userId = command.userId,
            amount = command.amount, method = command.method,
            idempotencyKey = command.idempotencyKey,
        )
        val saved = paymentRepository.save(payment)

        // ④ Idempotency Key DB 저장 (unique constraint가 race condition 최후 보루)
        val record = idempotencyKeyRepository.saveIfAbsent(
            IdempotencyRecord(key = command.idempotencyKey, paymentId = saved.id!!)
        )
        if (record == null) {
            // race로 다른 요청이 먼저 통과 — 그쪽 결과를 따른다
            val existingId = idempotencyKeyRepository.findByKey(command.idempotencyKey)!!.paymentId
            val existing = paymentRepository.findById(existingId)!!
            return PreparedPayment(existing, alreadyProcessed = true)
        }

        // ⑤ Redis 캐시 적재 (24h)
        idempotencyCache.put(command.idempotencyKey, saved.id!!, IDEMPOTENCY_TTL_SECONDS)

        return PreparedPayment(saved, alreadyProcessed = false)
    }

    companion object {
        private const val IDEMPOTENCY_TTL_SECONDS = 24L * 60 * 60
    }
}

data class PreparedPayment(
    val payment: Payment,
    val alreadyProcessed: Boolean,
)
