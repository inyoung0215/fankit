package com.fankit.payment.domain.model

// sealed class — 결과 종류가 컴파일 타임에 닫혀있어 when 표현식에서 누락 방지
sealed class PaymentResult {
    data class Success(val paymentId: Long, val amount: Int, val pgTransactionId: String) : PaymentResult()
    data class Failure(val paymentId: Long?, val reason: String) : PaymentResult()
}
