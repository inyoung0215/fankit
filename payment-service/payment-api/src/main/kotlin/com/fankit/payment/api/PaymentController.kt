package com.fankit.payment.api

import com.fankit.common.response.ApiResponse
import com.fankit.payment.api.dto.CreatePaymentRequest
import com.fankit.payment.api.dto.CreatePaymentResponse
import com.fankit.payment.api.dto.PaymentResponse
import com.fankit.payment.domain.port.inbound.CreatePaymentUseCase
import com.fankit.payment.domain.port.inbound.GetPaymentUseCase
import com.fankit.payment.domain.port.inbound.RefundPaymentUseCase
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/payments")
class PaymentController(
    private val createPaymentUseCase: CreatePaymentUseCase,
    private val getPaymentUseCase: GetPaymentUseCase,
    private val refundPaymentUseCase: RefundPaymentUseCase,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        // X-Idempotency-Key: 클라이언트가 발급한 UUID — 동일 키 재요청 시 이전 결과 반환
        @RequestHeader("X-Idempotency-Key") @NotBlank idempotencyKey: String,
        @RequestBody @Valid request: CreatePaymentRequest,
    ): ApiResponse<CreatePaymentResponse> =
        ApiResponse.success(
            CreatePaymentResponse.from(
                createPaymentUseCase.create(request.toCommand(idempotencyKey))
            )
        )

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): ApiResponse<PaymentResponse> =
        ApiResponse.success(PaymentResponse.from(getPaymentUseCase.getById(id)))

    @PostMapping("/{id}/refund")
    fun refund(@PathVariable id: Long): ApiResponse<PaymentResponse> =
        ApiResponse.success(PaymentResponse.from(refundPaymentUseCase.refund(id)))
}
