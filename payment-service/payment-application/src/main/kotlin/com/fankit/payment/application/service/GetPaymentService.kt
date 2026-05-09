package com.fankit.payment.application.service

import com.fankit.payment.domain.exception.PaymentNotFoundException
import com.fankit.payment.domain.model.Payment
import com.fankit.payment.domain.port.inbound.GetPaymentUseCase
import com.fankit.payment.domain.port.outbound.PaymentRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetPaymentService(
    private val paymentRepository: PaymentRepository,
) : GetPaymentUseCase {

    @Transactional(readOnly = true)
    override fun getById(id: Long): Payment =
        paymentRepository.findById(id) ?: throw PaymentNotFoundException(id)
}
