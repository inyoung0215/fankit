package com.fankit.settlement.domain.port.outbound

import com.fankit.settlement.domain.model.Reconciliation
import java.time.LocalDate

interface ReconciliationRepository {
    fun saveAll(items: List<Reconciliation>): List<Reconciliation>
    fun findAllByPeriod(period: LocalDate): List<Reconciliation>
}
