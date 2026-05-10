package com.fankit.settlement.domain.port.outbound

import com.fankit.settlement.domain.model.Settlement
import java.time.LocalDate

interface SettlementRepository {
    fun save(settlement: Settlement): Settlement
    fun findById(id: Long): Settlement?
    fun findByCreatorIdAndPeriod(creatorId: Long, period: LocalDate): Settlement?
    fun findAllByCreatorId(creatorId: Long): List<Settlement>
}
