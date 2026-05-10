package com.fankit.settlement.domain.port.inbound

import com.fankit.settlement.domain.model.ReconciliationStatus
import java.time.LocalDate

interface RunReconciliationUseCase {
    fun run(period: LocalDate): ReconciliationSummary
}

data class ReconciliationSummary(
    val period: LocalDate,
    val totalChecked: Int,
    val countByStatus: Map<ReconciliationStatus, Int>,
)
