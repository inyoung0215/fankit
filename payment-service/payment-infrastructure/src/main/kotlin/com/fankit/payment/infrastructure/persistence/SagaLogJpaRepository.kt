package com.fankit.payment.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface SagaLogJpaRepository : JpaRepository<SagaLogJpaEntity, Long> {
    // 같은 sagaId의 가장 최근 row를 가져온다 (saga 진행 중에 여러 row가 적재될 수 있음)
    fun findFirstBySagaIdOrderByIdDesc(sagaId: String): SagaLogJpaEntity?
}
