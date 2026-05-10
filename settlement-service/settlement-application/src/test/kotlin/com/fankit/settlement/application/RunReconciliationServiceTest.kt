package com.fankit.settlement.application

import com.fankit.settlement.domain.model.PaymentRecord
import com.fankit.settlement.domain.model.Reconciliation
import com.fankit.settlement.domain.model.ReconciliationStatus
import com.fankit.settlement.domain.port.outbound.PaymentRecordRepository
import com.fankit.settlement.domain.port.outbound.PgSettlementDataProvider
import com.fankit.settlement.domain.port.outbound.PgSettlementItem
import com.fankit.settlement.domain.port.outbound.ReconciliationRepository
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate

class RunReconciliationServiceTest {

    private val paymentRecordRepository: PaymentRecordRepository = mockk()
    private val pgProvider: PgSettlementDataProvider = mockk()
    private val reconciliationRepository: ReconciliationRepository = mockk()
    private val service = RunReconciliationService(paymentRecordRepository, pgProvider, reconciliationRepository)

    private fun record(paymentId: Long, amount: Int) = PaymentRecord(
        paymentId = paymentId, creatorId = 1L, goodsId = "g1",
        amount = amount, approvedAt = Instant.now(),
    )

    @Test
    fun `4가지 케이스 — MATCHED, MISMATCH, MISSING_INTERNAL, MISSING_PG 모두 분류`() {
        val period = LocalDate.of(2026, 5, 9)

        // 내부: paymentId 1, 2, 3
        // PG  : paymentId 1(같음), 2(금액 다름), 4(내부에 없음)  → 3은 PG에 없음
        val internalRecords = listOf(record(1L, 10_000), record(2L, 20_000), record(3L, 30_000))
        val pgItems = listOf(
            PgSettlementItem("pg1", 1L, 10_000),       // MATCHED
            PgSettlementItem("pg2", 2L, 99_999),       // AMOUNT_MISMATCH
            PgSettlementItem("pg4", 4L, 40_000),       // MISSING_INTERNAL
        )

        every { paymentRecordRepository.countUnsettledBetween(any(), any()) } returns 3L
        every { paymentRecordRepository.findUnsettledBetween(any(), any(), any(), any()) } returns internalRecords
        every { pgProvider.fetch(period) } returns pgItems

        val savedSlot = slot<List<Reconciliation>>()
        every { reconciliationRepository.saveAll(capture(savedSlot)) } answers { firstArg() }

        val summary = service.run(period)

        summary.totalChecked shouldBe 4
        summary.countByStatus[ReconciliationStatus.MATCHED] shouldBe 1
        summary.countByStatus[ReconciliationStatus.AMOUNT_MISMATCH] shouldBe 1
        summary.countByStatus[ReconciliationStatus.MISSING_INTERNAL] shouldBe 1
        summary.countByStatus[ReconciliationStatus.MISSING_PG] shouldBe 1
    }
}
