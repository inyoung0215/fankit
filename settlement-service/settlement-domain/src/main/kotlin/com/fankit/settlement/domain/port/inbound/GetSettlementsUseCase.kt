package com.fankit.settlement.domain.port.inbound

import com.fankit.settlement.domain.model.Settlement
import com.fankit.settlement.domain.model.SettlementDetail

interface GetSettlementsUseCase {
    fun byCreator(creatorId: Long): List<Settlement>
    fun detailsOf(settlementId: Long): SettlementWithDetails
}

data class SettlementWithDetails(
    val settlement: Settlement,
    val details: List<SettlementDetail>,
)
