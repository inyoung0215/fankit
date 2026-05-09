package com.fankit.payment.domain.model

import com.fankit.payment.domain.exception.IllegalPaymentStateException
import java.time.Instant

// Payment aggregate root
data class Payment(
    val id: Long? = null,
    val orderId: Long,
    val userId: Long,
    val amount: Int,
    val method: PaymentMethod,
    val status: PaymentStatus,
    val idempotencyKey: String,
    val pgTransactionId: String? = null,
    val failureReason: String? = null,
    val approvedAt: Instant? = null,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
) {
    fun approve(pgTxId: String, approvedAt: Instant = Instant.now()): Payment {
        check(status == PaymentStatus.PENDING) {
            throw IllegalPaymentStateException(status, PaymentStatus.APPROVED)
        }
        return copy(
            status = PaymentStatus.APPROVED,
            pgTransactionId = pgTxId,
            approvedAt = approvedAt,
        )
    }

    fun fail(reason: String): Payment {
        check(status == PaymentStatus.PENDING || status == PaymentStatus.APPROVED) {
            throw IllegalPaymentStateException(status, PaymentStatus.FAILED)
        }
        return copy(status = PaymentStatus.FAILED, failureReason = reason)
    }

    fun cancel(): Payment {
        check(status == PaymentStatus.PENDING) {
            throw IllegalPaymentStateException(status, PaymentStatus.CANCELLED)
        }
        return copy(status = PaymentStatus.CANCELLED)
    }

    fun refund(): Payment {
        check(status == PaymentStatus.APPROVED) {
            throw IllegalPaymentStateException(status, PaymentStatus.REFUNDED)
        }
        return copy(status = PaymentStatus.REFUNDED)
    }

    companion object {
        fun create(
            orderId: Long,
            userId: Long,
            amount: Int,
            method: PaymentMethod,
            idempotencyKey: String,
        ): Payment {
            require(amount in 100..10_000_000) { "결제 금액은 100원~1000만원 범위여야 한다" }
            require(idempotencyKey.isNotBlank()) { "Idempotency Key는 필수" }
            return Payment(
                orderId = orderId, userId = userId, amount = amount, method = method,
                status = PaymentStatus.PENDING, idempotencyKey = idempotencyKey,
            )
        }
    }
}
