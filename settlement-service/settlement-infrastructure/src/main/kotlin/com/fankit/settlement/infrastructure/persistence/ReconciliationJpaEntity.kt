package com.fankit.settlement.infrastructure.persistence

import com.fankit.settlement.domain.model.Reconciliation
import com.fankit.settlement.domain.model.ReconciliationStatus
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
import java.time.LocalDate

@Entity
@Table(
    name = "reconciliations",
    indexes = [
        Index(name = "idx_recon_period_status", columnList = "period,status"),
    ],
)
class ReconciliationJpaEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var period: LocalDate,

    @Column(name = "payment_id", nullable = false)
    var paymentId: Long,

    @Column(name = "internal_amount")
    var internalAmount: Int? = null,

    @Column(name = "pg_amount")
    var pgAmount: Int? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var status: ReconciliationStatus,

    @Column(name = "checked_at", nullable = false)
    var checkedAt: Instant,
) {
    fun toDomain() = Reconciliation(id, period, paymentId, internalAmount, pgAmount, status, checkedAt)

    companion object {
        fun fromDomain(r: Reconciliation) = ReconciliationJpaEntity(
            r.id, r.period, r.paymentId, r.internalAmount, r.pgAmount, r.status, r.checkedAt,
        )
    }
}
