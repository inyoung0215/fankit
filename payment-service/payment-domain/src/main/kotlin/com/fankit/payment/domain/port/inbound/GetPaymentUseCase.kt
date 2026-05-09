package com.fankit.payment.domain.port.inbound

import com.fankit.payment.domain.model.Payment

interface GetPaymentUseCase {
    fun getById(id: Long): Payment
}
