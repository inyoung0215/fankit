package com.fankit.payment.domain.port.outbound

import com.fankit.payment.domain.model.SagaLog

interface SagaLogRepository {
    fun save(log: SagaLog): SagaLog
    fun findBySagaId(sagaId: String): SagaLog?
}
