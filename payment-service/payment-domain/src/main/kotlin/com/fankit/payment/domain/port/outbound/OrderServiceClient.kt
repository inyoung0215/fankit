package com.fankit.payment.domain.port.outbound

// Order Service 호출 추상화 (실제론 Spring Cloud OpenFeign 또는 RestClient로 구현)
// Saga의 step 2 / 보상 단계에서 사용
interface OrderServiceClient {
    fun confirm(orderId: Long)   // 주문 상태 → PAID
    fun cancel(orderId: Long)    // 보상: 주문 상태 → CANCELLED
}
