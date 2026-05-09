package com.fankit.payment.domain.port.outbound

// 도메인 객체 → Outbox payload(JSON 문자열) 직렬화 추상화
// (Jackson을 도메인이 알 필요 없게 분리 — 인프라가 구현)
interface EventPayloadSerializer {
    fun serialize(payload: Any): String
}
