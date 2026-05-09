package com.fankit.payment.domain.port.inbound

import com.fankit.payment.domain.model.PaymentMethod
import com.fankit.payment.domain.model.PaymentResult

interface CreatePaymentUseCase {
    fun create(command: CreatePaymentCommand): PaymentResult
}

data class CreatePaymentCommand(
    val orderId: Long,
    val userId: Long,
    val amount: Int,
    val method: PaymentMethod,
    val idempotencyKey: String,                        // X-Idempotency-Key 헤더에서 주입
    val items: List<StockLineCommand>,                  // 재고 차감 step에 사용
)

data class StockLineCommand(val goodsId: String, val quantity: Int)
