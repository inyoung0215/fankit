package com.fankit.payment.infrastructure.messaging

import com.fankit.payment.domain.port.outbound.EventPayloadSerializer
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component

@Component
class JacksonEventPayloadSerializer(
    private val objectMapper: ObjectMapper,
) : EventPayloadSerializer {
    override fun serialize(payload: Any): String = objectMapper.writeValueAsString(payload)
}
