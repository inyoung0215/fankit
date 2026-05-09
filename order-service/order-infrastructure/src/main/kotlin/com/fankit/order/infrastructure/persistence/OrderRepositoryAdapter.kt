package com.fankit.order.infrastructure.persistence

import com.fankit.order.domain.model.Order
import com.fankit.order.domain.port.outbound.OrderRepository
import org.springframework.stereotype.Repository

@Repository
class OrderRepositoryAdapter(
    private val jpaRepository: OrderJpaRepository,
) : OrderRepository {

    override fun save(order: Order): Order =
        jpaRepository.save(OrderJpaEntity.fromDomain(order)).toDomain()

    override fun findById(id: Long): Order? =
        jpaRepository.findById(id).orElse(null)?.toDomain()
}
