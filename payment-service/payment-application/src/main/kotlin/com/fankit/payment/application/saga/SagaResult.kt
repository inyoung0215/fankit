package com.fankit.payment.application.saga

sealed class SagaResult {
    data class Success(val pgTransactionId: String) : SagaResult()
    data class Failure(val reason: String, val pgTransactionId: String? = null) : SagaResult()
}
