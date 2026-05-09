package com.fankit.payment.infrastructure.outbox

import com.fankit.payment.domain.port.outbound.OutboxEventRepository
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

// Transactional Outbox의 relay
//
// 주기적으로 PENDING 이벤트를 읽어 Kafka 발행 → 성공한 것만 PUBLISHED로 마킹
//
// 토픽 매핑: eventType → kafka topic 그대로 (예: "payment.completed")
//
// 주의:
//   - Kafka 발행 실패해도 status는 PENDING으로 남으니 다음 주기에 재시도 (at-least-once)
//   - 컨슈머 측에서 멱등 처리 필요 (이벤트에 paymentId가 키로 들어있어 중복 검출 가능)
@Component
class OutboxRelayScheduler(
    private val outboxEventRepository: OutboxEventRepository,
    private val kafkaTemplate: KafkaTemplate<String, String>,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelay = 1000)   // 1초 주기
    @Transactional
    fun relay() {
        val pending = outboxEventRepository.findPending(BATCH_SIZE)
        if (pending.isEmpty()) return

        for (event in pending) {
            try {
                kafkaTemplate.send(event.eventType, event.aggregateId, event.payload).get()
                outboxEventRepository.markPublished(event.id!!)
            } catch (e: Exception) {
                log.error("Outbox relay 실패 id={} type={} reason={}", event.id, event.eventType, e.message)
                // 다음 주기에 재시도 — markPublished 안 부르면 status는 PENDING 유지
            }
        }
    }

    companion object {
        private const val BATCH_SIZE = 100
    }
}
