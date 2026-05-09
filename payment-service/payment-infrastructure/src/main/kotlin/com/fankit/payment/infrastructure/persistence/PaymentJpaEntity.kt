package com.fankit.payment.infrastructure.persistence

import com.fankit.common.entity.BaseEntity
import com.fankit.payment.domain.model.Payment
import com.fankit.payment.domain.model.PaymentMethod
import com.fankit.payment.domain.model.PaymentStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(
    name = "payments",
    indexes = [
        Index(name = "idx_payments_order_id", columnList = "order_id"),
        Index(name = "idx_payments_idempotency_key", columnList = "idempotency_key", unique = true),
    ],
)
class PaymentJpaEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "order_id", nullable = false)
    var orderId: Long,

    @Column(name = "user_id", nullable = false)
    var userId: Long,

    @Column(nullable = false)
    var amount: Int,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var method: PaymentMethod,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: PaymentStatus,

    @Column(name = "idempotency_key", nullable = false, length = 100, unique = true)
    var idempotencyKey: String,

    @Column(name = "pg_transaction_id", length = 100)
    var pgTransactionId: String? = null,

    @Column(name = "failure_reason", length = 500)
    var failureReason: String? = null,

    @Column(name = "approved_at")
    var approvedAt: Instant? = null,
) : BaseEntity() {

    // 단순화: BaseEntity의 시간 필드는 도메인에 전달하지 않음 (필요 시 별도 조회)
    fun toDomain(): Payment = Payment(
        id = id, orderId = orderId, userId = userId, amount = amount,
        method = method, status = status, idempotencyKey = idempotencyKey,
        pgTransactionId = pgTransactionId, failureReason = failureReason,
        approvedAt = approvedAt,
    )

    companion object {
        fun fromDomain(p: Payment): PaymentJpaEntity = PaymentJpaEntity(
            id = p.id, orderId = p.orderId, userId = p.userId, amount = p.amount,
            method = p.method, status = p.status, idempotencyKey = p.idempotencyKey,
            pgTransactionId = p.pgTransactionId, failureReason = p.failureReason,
            approvedAt = p.approvedAt,
        )
    }
}
