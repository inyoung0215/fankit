package com.fankit.settlement.infrastructure.batch

import com.fankit.settlement.domain.model.PaymentRecord
import com.fankit.settlement.domain.port.outbound.PaymentRecordRepository
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.ItemReader
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.ZoneId

// chunk마다 PaymentRecord 100건씩 fetch (paymentId cursor 기반 forward-only)
//
// 왜 cursor 기반?
//   초기 설계는 "settled=true 마킹이 chunk commit되므로 offset=0 반복"이었으나,
//   Reader의 다음 fetch와 writer의 markSettled commit 가시성 사이에 동일 record를
//   재-fetch하는 케이스가 통합 테스트에서 관측됨 (readCount=220 / 실제 130건).
//   paymentId > lastSeenId 조건으로 forward-only 보장 → settled 컬럼은 멱등성 보강용.
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
    private var lastSeenId: Long = 0L
    private var exhausted: Boolean = false

    override fun read(): PaymentRecord? {
        if (buffer.isEmpty() && !exhausted) {
            val period = LocalDate.parse(periodStr)
            val from = period.atStartOfDay(zone).toInstant()
            val to = period.plusDays(1).atStartOfDay(zone).toInstant()
            val next = paymentRecordRepository.findUnsettledAfter(from, to, lastSeenId, pageSize)
            if (next.isEmpty()) {
                exhausted = true
                return null
            }
            buffer.addAll(next)
            lastSeenId = next.last().paymentId
        }
        return buffer.removeFirstOrNull()
    }
}
