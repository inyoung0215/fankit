package com.fankit.settlement.domain

import com.fankit.settlement.domain.model.Settlement
import com.fankit.settlement.domain.model.SettlementDetail
import com.fankit.settlement.domain.model.SettlementStatus
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.LocalDate

class SettlementTest {

    @Test
    fun `aggregate는 detail의 합계를 계산하고 PROCESSING 상태로 시작`() {
        val details = listOf(
            SettlementDetail(paymentId = 1L, goodsId = "g1", salesAmount = 10_000, commissionAmount = 1000, netAmount = 9000),
            SettlementDetail(paymentId = 2L, goodsId = "g2", salesAmount = 20_000, commissionAmount = 2000, netAmount = 18_000),
        )
        val s = Settlement.aggregate(creatorId = 1L, period = LocalDate.of(2026, 5, 9), details = details)

        s.totalSales shouldBe 30_000L
        s.totalCommission shouldBe 3000L
        s.netAmount shouldBe 27_000L
        s.status shouldBe SettlementStatus.PROCESSING
    }

    @Test
    fun `complete는 COMPLETED로 전이`() {
        val s = Settlement(
            creatorId = 1L, period = LocalDate.now(),
            totalSales = 10_000, totalCommission = 1000, netAmount = 9000,
            status = SettlementStatus.PROCESSING,
        )
        s.complete().status shouldBe SettlementStatus.COMPLETED
    }

    @Test
    fun `aggregate에 detail이 비어있으면 예외`() {
        shouldThrow<IllegalArgumentException> {
            Settlement.aggregate(1L, LocalDate.now(), emptyList())
        }
    }
}
