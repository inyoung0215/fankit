package com.fankit.settlement.application

import com.fankit.settlement.domain.model.PaymentRecord
import com.fankit.settlement.domain.model.Reconciliation
import com.fankit.settlement.domain.model.ReconciliationStatus
import com.fankit.settlement.domain.port.inbound.ReconciliationSummary
import com.fankit.settlement.domain.port.inbound.RunReconciliationUseCase
import com.fankit.settlement.domain.port.outbound.PaymentRecordRepository
import com.fankit.settlement.domain.port.outbound.PgSettlementDataProvider
import com.fankit.settlement.domain.port.outbound.PgSettlementItem
import com.fankit.settlement.domain.port.outbound.ReconciliationRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

// 대사 — 내부 PaymentRecord vs PG 정산 데이터 비교
//
// 양쪽을 paymentId로 outer-join 후:
//   1) 둘 다 있고 금액 같음    → MATCHED
//   2) 둘 다 있고 금액 다름    → AMOUNT_MISMATCH (운영 개입)
//   3) PG에만 있음             → MISSING_INTERNAL (사기/누락)
//   4) 내부에만 있음           → MISSING_PG (PG 측 누락)
@Service
class RunReconciliationService(
    private val paymentRecordRepository: PaymentRecordRepository,
    private val pgSettlementDataProvider: PgSettlementDataProvider,
    private val reconciliationRepository: ReconciliationRepository,
) : RunReconciliationUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun run(period: LocalDate): ReconciliationSummary {
        val zone = ZoneId.of("Asia/Seoul")
        val from = period.atStartOfDay(zone).toInstant()
        val to = period.plusDays(1).atStartOfDay(zone).toInstant()

        val internalAll = readAllUnsettled(from, to)
        val pgItems = pgSettlementDataProvider.fetch(period)

        val internalMap = internalAll.associateBy { it.paymentId }
        val pgMap = pgItems.associateBy { it.paymentId }
        val allIds = (internalMap.keys + pgMap.keys).toSortedSet()

        val now = Instant.now()
        val results = allIds.map { id -> compareOne(period, id, internalMap[id], pgMap[id], now) }

        reconciliationRepository.saveAll(results)

        val countByStatus = results.groupingBy { it.status }.eachCount()
        log.info("[Reconciliation] period={} total={} byStatus={}", period, results.size, countByStatus)

        return ReconciliationSummary(period = period, totalChecked = results.size, countByStatus = countByStatus)
    }

    private fun readAllUnsettled(from: Instant, to: Instant): List<PaymentRecord> {
        // 대사는 settled 무관하게 모든 결제를 봐야 하지만 단순화: 단일 페이지로 모두 읽음
        // 실전에선 페이징 필수 (대량 시 OOM)
        val total = paymentRecordRepository.countUnsettledBetween(from, to)
        return paymentRecordRepository.findUnsettledBetween(from, to, limit = total.toInt(), offset = 0)
    }

    private fun compareOne(
        period: LocalDate, paymentId: Long,
        internal: PaymentRecord?, pg: PgSettlementItem?, checkedAt: Instant,
    ): Reconciliation {
        val status = when {
            internal == null && pg != null -> ReconciliationStatus.MISSING_INTERNAL
            internal != null && pg == null -> ReconciliationStatus.MISSING_PG
            internal!!.amount != pg!!.amount -> ReconciliationStatus.AMOUNT_MISMATCH
            else -> ReconciliationStatus.MATCHED
        }
        return Reconciliation(
            period = period, paymentId = paymentId,
            internalAmount = internal?.amount, pgAmount = pg?.amount,
            status = status, checkedAt = checkedAt,
        )
    }
}
