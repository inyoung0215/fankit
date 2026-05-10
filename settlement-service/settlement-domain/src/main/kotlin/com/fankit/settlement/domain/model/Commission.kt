package com.fankit.settlement.domain.model

import java.math.BigDecimal
import java.math.RoundingMode

// 수수료 정책 — 도메인 객체 (금액 계산 invariant 보존)
//
// rate = 0.10 → 10% 수수료
// 정책이 더 복잡해지면(굿즈 카테고리별 차등, 크리에이터 등급별 등) 별도 strategy로 확장
@JvmInline
value class Commission(val rate: BigDecimal) {
    init {
        require(rate >= BigDecimal.ZERO && rate <= BigDecimal.ONE) {
            "수수료율은 0.0~1.0 사이여야 한다 (현재: $rate)"
        }
    }

    fun applyTo(amount: Int): Int =
        (BigDecimal(amount) * rate).setScale(0, RoundingMode.HALF_UP).toInt()

    fun netOf(amount: Int): Int = amount - applyTo(amount)

    companion object {
        val DEFAULT = Commission(BigDecimal("0.10"))   // 기본 10%
    }
}
