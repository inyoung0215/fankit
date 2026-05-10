package com.fankit.settlement.infrastructure.reconciliation

import com.fankit.settlement.domain.port.outbound.PgSettlementDataProvider
import com.fankit.settlement.domain.port.outbound.PgSettlementItem
import com.fankit.settlement.infrastructure.persistence.PaymentRecordJpaRepository
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.ZoneId

// PG사가 매일 발송하는 정산 데이터 시뮬레이션
//
// 실전: SFTP/S3에서 CSV 다운로드 + 파싱
// Mock 전략: 내부 PaymentRecord 90%는 그대로 + 5%는 금액 다르게 + 5%는 누락
//          → 대사 결과로 MATCHED / AMOUNT_MISMATCH / MISSING_PG가 모두 나옴 (시연용)
@Component
class MockPgSettlementDataProvider(
    private val paymentRecordJpaRepository: PaymentRecordJpaRepository,
) : PgSettlementDataProvider {

    override fun fetch(period: LocalDate): List<PgSettlementItem> {
        val zone = ZoneId.of("Asia/Seoul")
        val from = period.atStartOfDay(zone).toInstant()
        val to = period.plusDays(1).atStartOfDay(zone).toInstant()

        val internal = paymentRecordJpaRepository.findUnsettledBetween(
            from, to, org.springframework.data.domain.PageRequest.of(0, 10000)
        )

        return internal.mapIndexedNotNull { index, record ->
            val mod = index % 20
            when {
                mod == 0 -> null                                                                    // 5% 누락 (MISSING_PG)
                mod == 1 -> PgSettlementItem("pg_${record.paymentId}", record.paymentId, record.amount + 100)  // 5% 금액 다름
                else -> PgSettlementItem("pg_${record.paymentId}", record.paymentId, record.amount) // 90% 일치
            }
        }
    }
}
