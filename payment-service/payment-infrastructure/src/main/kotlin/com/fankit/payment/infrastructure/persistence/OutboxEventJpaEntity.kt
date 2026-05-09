package com.fankit.payment.infrastructure.persistence

import com.fankit.payment.domain.model.OutboxEvent
import com.fankit.payment.domain.model.OutboxStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Lob
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(
    name = "outbox_events",
    indexes = [Index(name = "idx_outbox_status_id", columnList = "status,id")],
)
class OutboxEventJpaEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "aggregate_type", nullable = false, length = 50)
    var aggregateType: String,

    @Column(name = "aggregate_id", nullable = false, length = 50)
    var aggregateId: String,

    @Column(name = "event_type", nullable = false, length = 100)
    var eventType: String,

    @Lob
    @Column(nullable = false)
    var payload: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: OutboxStatus = OutboxStatus.PENDING,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant? = null,

    @Column(name = "published_at")
    var publishedAt: Instant? = null,
) {
    fun toDomain() = OutboxEvent(
        id = id, aggregateType = aggregateType, aggregateId = aggregateId,
        eventType = eventType, payload = payload, status = status,
        createdAt = createdAt, publishedAt = publishedAt,
    )

    companion object {
        fun fromDomain(e: OutboxEvent) = OutboxEventJpaEntity(
            id = e.id, aggregateType = e.aggregateType, aggregateId = e.aggregateId,
            eventType = e.eventType, payload = e.payload, status = e.status,
            publishedAt = e.publishedAt,
        )
    }
}
