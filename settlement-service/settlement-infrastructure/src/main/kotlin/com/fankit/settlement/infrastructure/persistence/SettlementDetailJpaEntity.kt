package com.fankit.settlement.infrastructure.persistence

import com.fankit.settlement.domain.model.SettlementDetail
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table

// settlementId는 nullable — Step 1에서 detail 먼저 저장하고
// Step 2 (Aggregate Tasklet)이 settlement aggregate row 생성 후 update로 채움
@Entity
@Table(
    name = "settlement_details",
    indexes = [
        Index(name = "idx_sd_settlement", columnList = "settlement_id"),
        Index(name = "idx_sd_creator_settled", columnList = "creator_id,settlement_id"),
    ],
)
class SettlementDetailJpaEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "settlement_id")
    var settlementId: Long? = null,

    @Column(name = "creator_id", nullable = false)
    var creatorId: Long,

    @Column(name = "payment_id", nullable = false)
    var paymentId: Long,

    @Column(name = "goods_id", nullable = false, length = 50)
    var goodsId: String,

    @Column(name = "sales_amount", nullable = false)
    var salesAmount: Int,

    @Column(name = "commission_amount", nullable = false)
    var commissionAmount: Int,

    @Column(name = "net_amount", nullable = false)
    var netAmount: Int,
) {
    fun toDomain() = SettlementDetail(
        id, settlementId, paymentId, goodsId, salesAmount, commissionAmount, netAmount,
    )
}
