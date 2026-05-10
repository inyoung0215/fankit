package com.fankit.settlement.infrastructure.persistence

import com.fankit.settlement.domain.model.PaymentRecord
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(
    name = "payment_records",
    indexes = [
        Index(name = "idx_pr_settled_approved", columnList = "settled,approved_at"),
        Index(name = "idx_pr_creator", columnList = "creator_id"),
    ],
)
class PaymentRecordJpaEntity(
    @Id
    @Column(name = "payment_id")
    var paymentId: Long,

    @Column(name = "creator_id", nullable = false)
    var creatorId: Long,

    @Column(name = "goods_id", nullable = false, length = 50)
    var goodsId: String,

    @Column(nullable = false)
    var amount: Int,

    @Column(name = "approved_at", nullable = false)
    var approvedAt: Instant,

    @Column(nullable = false)
    var settled: Boolean = false,
) {
    fun toDomain() = PaymentRecord(paymentId, creatorId, goodsId, amount, approvedAt, settled)

    companion object {
        fun fromDomain(p: PaymentRecord) = PaymentRecordJpaEntity(
            p.paymentId, p.creatorId, p.goodsId, p.amount, p.approvedAt, p.settled,
        )
    }
}
