package com.fankit.payment.domain.port.inbound

import com.fankit.payment.domain.model.Payment

interface RefundPaymentUseCase {
    fun refund(paymentId: Long): Payment
}
