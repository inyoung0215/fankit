package com.fankit.payment.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface IdempotencyKeyJpaRepository : JpaRepository<IdempotencyKeyJpaEntity, Long> {
    fun findByIdempotencyKey(key: String): IdempotencyKeyJpaEntity?
}
