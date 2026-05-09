package com.fankit.order.api

import com.fankit.common.response.ApiResponse
import com.fankit.order.api.dto.ConfirmOrderRequest
import com.fankit.order.api.dto.CreateOrderRequest
import com.fankit.order.api.dto.OrderResponse
import com.fankit.order.domain.port.inbound.CancelOrderUseCase
import com.fankit.order.domain.port.inbound.ConfirmOrderUseCase
import com.fankit.order.domain.port.inbound.CreateOrderUseCase
import com.fankit.order.domain.port.inbound.GetOrderUseCase
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/orders")
class OrderController(
    private val createOrderUseCase: CreateOrderUseCase,
    private val getOrderUseCase: GetOrderUseCase,
    private val confirmOrderUseCase: ConfirmOrderUseCase,
    private val cancelOrderUseCase: CancelOrderUseCase,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody @Valid request: CreateOrderRequest): ApiResponse<OrderResponse> =
        ApiResponse.success(OrderResponse.from(createOrderUseCase.create(request.toCommand())))

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): ApiResponse<OrderResponse> =
        ApiResponse.success(OrderResponse.from(getOrderUseCase.getById(id)))

    // Saga의 동기 RPC endpoint — Payment Service의 OrderServiceClient.confirm() 구현체가 호출
    @PostMapping("/{id}/confirm")
    fun confirm(@PathVariable id: Long, @RequestBody @Valid request: ConfirmOrderRequest): ApiResponse<Unit> {
        confirmOrderUseCase.confirm(id, request.paymentId)
        return ApiResponse.success()
    }

    // Saga 보상 endpoint
    @PostMapping("/{id}/cancel")
    fun cancel(@PathVariable id: Long): ApiResponse<Unit> {
        cancelOrderUseCase.cancel(id)
        return ApiResponse.success()
    }
}
