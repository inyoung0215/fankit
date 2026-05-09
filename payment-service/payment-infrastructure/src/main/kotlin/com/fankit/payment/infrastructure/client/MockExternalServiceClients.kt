package com.fankit.payment.infrastructure.client

import com.fankit.payment.domain.port.outbound.GoodsServiceClient
import com.fankit.payment.domain.port.outbound.OrderServiceClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

// Order/Goods Service 통신 Mock
// 실전: Spring Cloud OpenFeign 또는 RestClient — Eureka 통한 lb:// 라우팅
// 지금은 Saga 흐름 검증/시연만 위해 Mock으로 단순 로깅

@Component
class MockOrderServiceClient : OrderServiceClient {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun confirm(orderId: Long) {
        log.info("[MockOrder] confirm orderId={}", orderId)
    }

    override fun cancel(orderId: Long) {
        log.info("[MockOrder] cancel orderId={} (Saga 보상)", orderId)
    }
}

@Component
class MockGoodsServiceClient : GoodsServiceClient {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun decreaseStock(items: List<GoodsServiceClient.StockLine>) {
        log.info("[MockGoods] decrease stock items={}", items)
    }

    override fun restoreStock(items: List<GoodsServiceClient.StockLine>) {
        log.info("[MockGoods] restore stock items={} (Saga 보상)", items)
    }
}
