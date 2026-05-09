package com.fankit.order.domain.model

// 주문 상태 머신
//
//   PAYMENT_PENDING ──confirm()──▶ PAID ──ship()──▶ SHIPPING ──deliver()──▶ DELIVERED
//          │                        │
//          └─cancel()────────────▶ CANCELLED ◀──cancel()──┘
//
enum class OrderStatus {
    PAYMENT_PENDING,    // 주문 생성 직후 (결제 대기)
    PAID,               // 결제 완료 (Saga 동기 confirm 또는 Kafka payment.completed)
    SHIPPING,           // 배송 중
    DELIVERED,          // 배송 완료
    CANCELLED,          // 결제 실패 또는 사용자 취소
}
