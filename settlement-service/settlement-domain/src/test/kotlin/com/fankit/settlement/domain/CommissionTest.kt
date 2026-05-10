package com.fankit.settlement.domain

import com.fankit.settlement.domain.model.Commission
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class CommissionTest {

    @Test
    fun `10퍼센트 수수료 적용 — 반올림 HALF_UP`() {
        val c = Commission(BigDecimal("0.10"))
        c.applyTo(10_000) shouldBe 1000
        c.applyTo(9_999) shouldBe 1000   // 999.9 → HALF_UP → 1000
        c.applyTo(9_995) shouldBe 1000   // 999.5 → HALF_UP → 1000
        c.applyTo(9_994) shouldBe 999    // 999.4 → 999
    }

    @Test
    fun `netOf는 amount - commission`() {
        Commission(BigDecimal("0.10")).netOf(10_000) shouldBe 9000
    }

    @Test
    fun `수수료율은 0_0~1_0 범위`() {
        shouldThrow<IllegalArgumentException> { Commission(BigDecimal("-0.01")) }
        shouldThrow<IllegalArgumentException> { Commission(BigDecimal("1.01")) }
    }
}
