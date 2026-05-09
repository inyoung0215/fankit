package com.fankit.payment.domain.port.outbound

import com.fankit.payment.domain.model.OutboxEvent

interface OutboxEventRepository {
    fun save(event: OutboxEvent): OutboxEvent
    fun findPending(limit: Int): List<OutboxEvent>
    fun markPublished(id: Long)
}
