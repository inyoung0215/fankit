package com.fankit.settlement.domain.port.outbound

import com.fankit.settlement.domain.model.PaymentRecord
import java.time.Instant

interface PaymentRecordRepository {
    fun save(record: PaymentRecord): PaymentRecord
    fun findUnsettledBetween(from: Instant, to: Instant, limit: Int, offset: Int): List<PaymentRecord>
    fun countUnsettledBetween(from: Instant, to: Instant): Long
    fun markSettled(paymentIds: List<Long>)
}
