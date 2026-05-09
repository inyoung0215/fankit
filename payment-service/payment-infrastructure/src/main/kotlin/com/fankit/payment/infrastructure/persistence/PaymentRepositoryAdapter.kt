package com.fankit.payment.infrastructure.persistence

import com.fankit.payment.domain.model.Payment
import com.fankit.payment.domain.port.outbound.PaymentRepository
import org.springframework.stereotype.Repository

@Repository
class PaymentRepositoryAdapter(
    private val jpaRepository: PaymentJpaRepository,
) : PaymentRepository {

    override fun save(payment: Payment): Payment {
        // id가 있는 경우 update를 위해 기존 entity 로드 후 필드 갱신 (영속성 컨텍스트 활용)
        // 단순화: id 무관하게 fromDomain으로 새 entity 만들어 save (id 있으면 merge)
        return jpaRepository.save(PaymentJpaEntity.fromDomain(payment)).toDomain()
    }

    override fun findById(id: Long): Payment? =
        jpaRepository.findById(id).orElse(null)?.toDomain()

    override fun findByOrderId(orderId: Long): Payment? =
        jpaRepository.findByOrderId(orderId)?.toDomain()

    override fun findByOrderIdForUpdate(orderId: Long): Payment? =
        jpaRepository.findByOrderIdForUpdate(orderId)?.toDomain()
}
