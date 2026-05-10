package com.fankit.settlement.infrastructure.batch

import com.fankit.settlement.domain.model.PaymentRecord
import com.fankit.settlement.domain.port.outbound.PaymentRecordRepository
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.ItemReader
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.ZoneId

// chunk마다 PaymentRecord 100건씩 fetch
//
// 핵심: settled=true 마킹이 chunk 단위 commit되므로 다음 fetch에선 자동으로 제외됨
// → offset 관리 불필요 (LIMIT 100만 반복)
//
// @StepScope: Job이 시작될 때마다 새 인스턴스 + jobParameters 주입
@Component
@StepScope
class SettlementItemReader(
    @Value("#{jobParameters['period']}") private val periodStr: String,
    private val paymentRecordRepository: PaymentRecordRepository,
) : ItemReader<PaymentRecord> {

    private val zone = ZoneId.of("Asia/Seoul")
    private val pageSize = 100
    private val buffer: ArrayDeque<PaymentRecord> = ArrayDeque()

    override fun read(): PaymentRecord? {
        if (buffer.isEmpty()) {
            val period = LocalDate.parse(periodStr)
            val from = period.atStartOfDay(zone).toInstant()
            val to = period.plusDays(1).atStartOfDay(zone).toInstant()
            // settled가 chunk마다 갱신되므로 항상 offset=0으로 첫 페이지만 가져온다
            val next = paymentRecordRepository.findUnsettledBetween(from, to, pageSize, 0)
            if (next.isEmpty()) return null
            buffer.addAll(next)
        }
        return buffer.removeFirst()
    }
}
