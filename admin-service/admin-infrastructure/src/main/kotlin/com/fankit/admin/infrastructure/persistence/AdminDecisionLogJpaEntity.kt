package com.fankit.admin.infrastructure.persistence

import com.fankit.admin.domain.model.AdminDecisionLog
import com.fankit.admin.domain.model.ApprovalDecision
import com.fankit.admin.domain.model.ApprovalType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(
    name = "admin_decision_logs",
    indexes = [
        Index(name = "idx_decision_type_decided_at", columnList = "type,decided_at"),
        Index(name = "idx_decision_target", columnList = "type,target_id"),
    ],
)
class AdminDecisionLogJpaEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var type: ApprovalType,

    @Column(name = "target_id", nullable = false, length = 100)
    var targetId: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var decision: ApprovalDecision,

    @Column(name = "decided_by", nullable = false)
    var decidedBy: Long,

    @Column(length = 500)
    var reason: String? = null,

    @CreatedDate
    @Column(name = "decided_at", nullable = false, updatable = false)
    var decidedAt: Instant? = null,
) {
    fun toDomain() = AdminDecisionLog(id, type, targetId, decision, decidedBy, reason, decidedAt)

    companion object {
        fun fromDomain(d: AdminDecisionLog) = AdminDecisionLogJpaEntity(
            d.id, d.type, d.targetId, d.decision, d.decidedBy, d.reason,
        )
    }
}
