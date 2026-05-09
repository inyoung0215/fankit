package com.fankit.payment.infrastructure.persistence

import com.fankit.common.entity.BaseEntity
import com.fankit.payment.domain.model.SagaLog
import com.fankit.payment.domain.model.SagaStatus
import com.fankit.payment.domain.model.SagaStep
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table

@Entity
@Table(
    name = "saga_logs",
    indexes = [Index(name = "idx_saga_logs_saga_id", columnList = "saga_id")],
)
class SagaLogJpaEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "saga_id", nullable = false, length = 50)
    var sagaId: String,

    @Column(name = "payment_id")
    var paymentId: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var status: SagaStatus,

    @Enumerated(EnumType.STRING)
    @Column(name = "current_step", nullable = false, length = 30)
    var currentStep: SagaStep,

    @Column(name = "failure_reason", length = 1000)
    var failureReason: String? = null,
) : BaseEntity() {

    fun toDomain(): SagaLog = SagaLog(
        id = id, sagaId = sagaId, paymentId = paymentId, status = status,
        currentStep = currentStep, failureReason = failureReason,
    )

    companion object {
        fun fromDomain(s: SagaLog) = SagaLogJpaEntity(
            id = s.id, sagaId = s.sagaId, paymentId = s.paymentId,
            status = s.status, currentStep = s.currentStep, failureReason = s.failureReason,
        )
    }
}
