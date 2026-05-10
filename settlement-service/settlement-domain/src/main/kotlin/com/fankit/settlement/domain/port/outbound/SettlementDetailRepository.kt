package com.fankit.settlement.domain.port.outbound

import com.fankit.settlement.domain.model.SettlementDetail

interface SettlementDetailRepository {
    fun saveAll(details: List<SettlementDetail>): List<SettlementDetail>
    fun findAllBySettlementId(settlementId: Long): List<SettlementDetail>
}
