package com.fankit.payment.api.dto

import com.fankit.payment.domain.model.Payment
import com.fankit.payment.domain.model.PaymentMethod
import com.fankit.payment.domain.model.PaymentResult
import com.fankit.payment.domain.model.PaymentStatus
import com.fankit.payment.domain.port.inbound.CreatePaymentCommand
import com.fankit.payment.domain.port.inbound.StockLineCommand
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotEmpty

data class CreatePaymentRequest(
    @field:Min(1) val orderId: Long,
    @field:Min(1) val userId: Long,                      // TODO: JWT claim에서 추출하도록 추후 변경
    @field:Min(100) val amount: Int,
    val method: PaymentMethod,
    @field:Valid @field:NotEmpty val items: List<StockLineRequest>,
) {
    fun toCommand(idempotencyKey: String) = CreatePaymentCommand(
        orderId = orderId, userId = userId, amount = amount, method = method,
        idempotencyKey = idempotencyKey,
        items = items.map { StockLineCommand(it.goodsId, it.quantity) },
    )
}

data class StockLineRequest(
    val goodsId: String,
    @field:Min(1) val quantity: Int,
)

// PaymentResult 분기를 그대로 노출 — 클라이언트가 success/failure 판단
data class CreatePaymentResponse(
    val success: Boolean,
    val paymentId: Long?,
    val amount: Int? = null,
    val pgTransactionId: String? = null,
    val failureReason: String? = null,
) {
    companion object {
        fun from(r: PaymentResult): CreatePaymentResponse = when (r) {
            is PaymentResult.Success -> CreatePaymentResponse(
                success = true, paymentId = r.paymentId,
                amount = r.amount, pgTransactionId = r.pgTransactionId,
            )
            is PaymentResult.Failure -> CreatePaymentResponse(
                success = false, paymentId = r.paymentId, failureReason = r.reason,
            )
        }
    }
}

data class PaymentResponse(
    val id: Long,
    val orderId: Long,
    val userId: Long,
    val amount: Int,
    val method: PaymentMethod,
    val status: PaymentStatus,
    val pgTransactionId: String?,
    val failureReason: String?,
) {
    companion object {
        fun from(p: Payment) = PaymentResponse(
            id = p.id!!, orderId = p.orderId, userId = p.userId,
            amount = p.amount, method = p.method, status = p.status,
            pgTransactionId = p.pgTransactionId, failureReason = p.failureReason,
        )
    }
}
