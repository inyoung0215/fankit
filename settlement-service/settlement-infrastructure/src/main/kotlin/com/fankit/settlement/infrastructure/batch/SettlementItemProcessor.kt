package com.fankit.settlement.infrastructure.batch

import com.fankit.settlement.domain.model.Commission
import com.fankit.settlement.domain.model.PaymentRecord
import org.springframework.batch.item.ItemProcessor
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.math.BigDecimal

// 결제 1건 → 정산 명세 1건 (수수료 적용)
//
// 수수료율은 application.yml의 settlement.commission.rate에서 주입
// → 정책이 바뀌어도 코드 변경 없이 환경 변수로 조정 가능
@Component
class SettlementItemProcessor(
    @Value("\${settlement.commission.rate:0.10}") rate: String,
) : ItemProcessor<PaymentRecord, SettlementBatchItem> {

    private val commission = Commission(BigDecimal(rate))

    override fun process(item: PaymentRecord): SettlementBatchItem {
        val commissionAmount = commission.applyTo(item.amount)
        return SettlementBatchItem(
            paymentId = item.paymentId,
            creatorId = item.creatorId,
            goodsId = item.goodsId,
            salesAmount = item.amount,
            commissionAmount = commissionAmount,
            netAmount = item.amount - commissionAmount,
        )
    }
}
