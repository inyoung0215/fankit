package com.fankit.payment.infrastructure.persistence

import com.fankit.payment.domain.model.IdempotencyRecord
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import jakarta.persistence.EntityListeners
import java.time.Instant

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(
    name = "idempotency_keys",
    indexes = [Index(name = "idx_idem_key", columnList = "idempotency_key", unique = true)],
)
class IdempotencyKeyJpaEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "idempotency_key", nullable = false, length = 100, unique = true)
    var idempotencyKey: String,

    @Column(name = "payment_id", nullable = false)
    var paymentId: Long,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant? = null,
) {
    fun toDomain() = IdempotencyRecord(idempotencyKey, paymentId, createdAt)

    companion object {
        fun fromDomain(r: IdempotencyRecord) =
            IdempotencyKeyJpaEntity(idempotencyKey = r.key, paymentId = r.paymentId)
    }
}
