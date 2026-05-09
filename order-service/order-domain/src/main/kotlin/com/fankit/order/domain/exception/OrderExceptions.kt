package com.fankit.order.domain.exception

import com.fankit.order.domain.model.OrderStatus

class OrderNotFoundException(id: Long) :
    RuntimeException("주문을 찾을 수 없습니다: $id")

class IllegalOrderStateException(from: OrderStatus, to: OrderStatus) :
    RuntimeException("주문 상태 전이 불가: $from → $to")
