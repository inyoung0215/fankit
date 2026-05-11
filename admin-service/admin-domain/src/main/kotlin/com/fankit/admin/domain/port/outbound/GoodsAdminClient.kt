package com.fankit.admin.domain.port.outbound

// Goods Service에 대한 admin 작업 추상화
// 실전: Spring Cloud OpenFeign + Eureka로 lb://goods-service 호출
// 현 단계: Mock — 호출만 로깅
interface GoodsAdminClient {
    fun approveGoods(goodsId: String)
    fun rejectGoods(goodsId: String, reason: String)
}
