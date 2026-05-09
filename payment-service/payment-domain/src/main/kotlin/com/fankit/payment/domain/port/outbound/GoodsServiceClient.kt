package com.fankit.payment.domain.port.outbound

// Goods Service 호출 추상화
// Saga의 step 3 / 보상 단계 — 분산 락은 호출 측(Saga) 또는 Goods Service 양쪽 가능
// FanKit은 Goods Service에서 Redisson 분산 락 적용 (재고는 Goods의 책임)
interface GoodsServiceClient {
    data class StockLine(val goodsId: String, val quantity: Int)

    fun decreaseStock(items: List<StockLine>)
    fun restoreStock(items: List<StockLine>)   // 보상
}
