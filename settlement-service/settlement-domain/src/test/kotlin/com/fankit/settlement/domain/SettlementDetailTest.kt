package com.fankit.settlement.domain

import com.fankit.settlement.domain.model.Commission
import com.fankit.settlement.domain.model.SettlementDetail
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class SettlementDetailTest {

    @Test
    fun `calculate는 수수료 적용 후 net = sales - commission invariant 보장`() {
        val d = SettlementDetail.calculate(
            paymentId = 1L, goodsId = "g1",
            salesAmount = 10_000, commission = Commission(BigDecimal("0.10")),
        )
        d.salesAmount shouldBe 10_000
        d.commissionAmount shouldBe 1000
        d.netAmount shouldBe 9000
    }

    @Test
    fun `net 불일치 시 직접 생성하면 invariant 위반 예외`() {
        shouldThrow<IllegalArgumentException> {
            SettlementDetail(
                paymentId = 1L, goodsId = "g1",
                salesAmount = 10_000, commissionAmount = 1000, netAmount = 8888,  // wrong
            )
        }
    }
}
