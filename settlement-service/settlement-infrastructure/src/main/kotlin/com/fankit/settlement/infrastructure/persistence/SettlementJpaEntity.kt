package com.fankit.settlement.infrastructure.persistence

import com.fankit.common.entity.BaseEntity
import com.fankit.settlement.domain.model.Settlement
import com.fankit.settlement.domain.model.SettlementStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDate

@Entity
@Table(
    name = "settlements",
    uniqueConstraints = [
        UniqueConstraint(name = "uq_settlements_creator_period", columnNames = ["creator_id", "period"]),
    ],
    indexes = [Index(name = "idx_settlements_creator", columnList = "creator_id")],
)
class SettlementJpaEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "creator_id", nullable = false)
    var creatorId: Long,

    @Column(nullable = false)
    var period: LocalDate,

    @Column(name = "total_sales", nullable = false)
    var totalSales: Long,

    @Column(name = "total_commission", nullable = false)
    var totalCommission: Long,

    @Column(name = "net_amount", nullable = false)
    var netAmount: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: SettlementStatus,
) : BaseEntity() {
    fun toDomain() = Settlement(id, creatorId, period, totalSales, totalCommission, netAmount, status)

    companion object {
        fun fromDomain(s: Settlement) = SettlementJpaEntity(
            s.id, s.creatorId, s.period, s.totalSales, s.totalCommission, s.netAmount, s.status,
        )
    }
}
