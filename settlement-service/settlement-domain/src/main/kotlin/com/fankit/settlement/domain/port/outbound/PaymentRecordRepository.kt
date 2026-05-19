package com.fankit.settlement.domain.port.outbound

import com.fankit.settlement.domain.model.PaymentRecord
import java.time.Instant

interface PaymentRecordRepository {
    fun save(record: PaymentRecord): PaymentRecord

    /**
     * 기간 내 unsettled 레코드를 offset/limit 기반으로 조회.
     * 주로 Reconciliation(대사)에서 전체 데이터 비교 시 사용.
     * Settlement Batch reader에서는 cursor 기반 [findUnsettledAfter]를 사용한다.
     */
    fun findUnsettledBetween(from: Instant, to: Instant, limit: Int, offset: Int): List<PaymentRecord>

    /**
     * paymentId 오름차순 cursor 기반 pagination.
     * Reader가 lastSeenId를 유지하면서 forward-only로 진행하므로
     * settled 컬럼 마킹의 트랜잭션 commit 가시성에 의존하지 않음.
     */
    fun findUnsettledAfter(from: Instant, to: Instant, afterPaymentId: Long, limit: Int): List<PaymentRecord>

    fun countUnsettledBetween(from: Instant, to: Instant): Long
    fun markSettled(paymentIds: List<Long>)
}
