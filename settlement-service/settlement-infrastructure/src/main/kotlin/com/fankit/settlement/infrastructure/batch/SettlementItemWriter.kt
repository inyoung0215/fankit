package com.fankit.settlement.infrastructure.batch

import com.fankit.settlement.domain.port.outbound.PaymentRecordRepository
import com.fankit.settlement.infrastructure.persistence.SettlementDetailJpaEntity
import com.fankit.settlement.infrastructure.persistence.SettlementDetailJpaRepository
import org.springframework.batch.item.Chunk
import org.springframework.batch.item.ItemWriter
import org.springframework.stereotype.Component

// Chunk(=100) 단위 트랜잭션:
//   1) SettlementDetail 100건 INSERT (settlementId는 null — Step 2에서 채움)
//   2) 같은 chunk의 paymentId들에 대해 settled=true UPDATE (멱등성 보장)
//
// chunk 트랜잭션이 commit되면 (1)+(2)가 원자적 → 부분 실패 시 자동 롤백되어 재시도
@Component
class SettlementItemWriter(
    private val settlementDetailJpaRepository: SettlementDetailJpaRepository,
    private val paymentRecordRepository: PaymentRecordRepository,
) : ItemWriter<SettlementBatchItem> {

    override fun write(chunk: Chunk<out SettlementBatchItem>) {
        val items = chunk.items
        if (items.isEmpty()) return

        val entities = items.map {
            SettlementDetailJpaEntity(
                creatorId = it.creatorId,
                paymentId = it.paymentId,
                goodsId = it.goodsId,
                salesAmount = it.salesAmount,
                commissionAmount = it.commissionAmount,
                netAmount = it.netAmount,
            )
        }
        settlementDetailJpaRepository.saveAll(entities)

        paymentRecordRepository.markSettled(items.map { it.paymentId })
    }
}
