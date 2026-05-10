package com.fankit.settlement.infrastructure.persistence

import com.fankit.settlement.domain.model.SettlementDetail
import com.fankit.settlement.domain.port.outbound.SettlementDetailRepository
import org.springframework.stereotype.Repository

// 도메인 SettlementDetail은 creatorId 필드가 없으므로 adapter가 변환 시 잃어버림
// 배치는 SettlementDetailWithCreator(infrastructure 전용 DTO)로 처리하고
// 외부에 노출 시에만 도메인 SettlementDetail로 변환
@Repository
class SettlementDetailRepositoryAdapter(
    private val jpaRepository: SettlementDetailJpaRepository,
) : SettlementDetailRepository {

    override fun saveAll(details: List<SettlementDetail>): List<SettlementDetail> {
        // 배치는 직접 jpaRepository를 사용하므로 이 경로는 거의 안 탐
        // 도메인에서 호출하면 creatorId가 비어 entity 생성 불가 → 미지원
        throw UnsupportedOperationException("saveAll은 배치 내부 SettlementDetailWithCreator 경로 사용")
    }

    override fun findAllBySettlementId(settlementId: Long): List<SettlementDetail> =
        jpaRepository.findAllBySettlementId(settlementId).map { it.toDomain() }
}
