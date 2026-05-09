package com.fankit.order.application

import com.fankit.order.domain.port.inbound.CancelOrderUseCase
import com.fankit.order.domain.port.inbound.ConfirmOrderUseCase
import org.springframework.stereotype.Service

// Kafka 이벤트 → 도메인 액션 매핑
// 인프라 컴포넌트(@KafkaListener)는 이 핸들러를 호출하여 비즈니스 로직 분리
//
// 면접 어필: "이벤트 컨슈머 멱등 처리 어떻게?" → ConfirmOrderUseCase가 이미 PAID면 no-op
//          → at-least-once 재전송에도 안전 + Saga 동기 RPC와 두 경로 양립 OK
@Service
class PaymentEventHandler(
    private val confirmOrderUseCase: ConfirmOrderUseCase,
    private val cancelOrderUseCase: CancelOrderUseCase,
) {
    fun onPaymentCompleted(orderId: Long, paymentId: Long) {
        confirmOrderUseCase.confirm(orderId, paymentId)
    }

    fun onPaymentFailed(orderId: Long) {
        cancelOrderUseCase.cancel(orderId)
    }
}
