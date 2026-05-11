package com.fankit.admin.infrastructure.client

import com.fankit.admin.domain.port.outbound.GoodsAdminClient
import com.fankit.admin.domain.port.outbound.UserAdminClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

// 실전: Spring Cloud OpenFeign 또는 RestClient + Eureka 통한 lb:// 라우팅
//   예) goodsAdminClient → POST lb://goods-service/api/v1/admin/goods/{id}/approve
// 현 단계: Mock — 호출만 로깅 (다른 서비스 안 띄워도 작동)

@Component
class MockGoodsAdminClient : GoodsAdminClient {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun approveGoods(goodsId: String) {
        log.info("[MockGoodsAdmin] approveGoods goodsId={}", goodsId)
    }

    override fun rejectGoods(goodsId: String, reason: String) {
        log.info("[MockGoodsAdmin] rejectGoods goodsId={} reason={}", goodsId, reason)
    }
}

@Component
class MockUserAdminClient : UserAdminClient {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun promoteToCreator(userId: Long) {
        log.info("[MockUserAdmin] promoteToCreator userId={}", userId)
    }
}
