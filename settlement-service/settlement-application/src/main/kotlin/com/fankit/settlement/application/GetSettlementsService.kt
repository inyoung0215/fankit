package com.fankit.settlement.application

import com.fankit.settlement.domain.exception.SettlementNotFoundException
import com.fankit.settlement.domain.model.Settlement
import com.fankit.settlement.domain.port.inbound.GetSettlementsUseCase
import com.fankit.settlement.domain.port.inbound.SettlementWithDetails
import com.fankit.settlement.domain.port.outbound.SettlementDetailRepository
import com.fankit.settlement.domain.port.outbound.SettlementRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetSettlementsService(
    private val settlementRepository: SettlementRepository,
    private val settlementDetailRepository: SettlementDetailRepository,
) : GetSettlementsUseCase {

    @Transactional(readOnly = true)
    override fun byCreator(creatorId: Long): List<Settlement> =
        settlementRepository.findAllByCreatorId(creatorId)

    @Transactional(readOnly = true)
    override fun detailsOf(settlementId: Long): SettlementWithDetails {
        val settlement = settlementRepository.findById(settlementId)
            ?: throw SettlementNotFoundException(settlementId)
        val details = settlementDetailRepository.findAllBySettlementId(settlementId)
        return SettlementWithDetails(settlement, details)
    }
}
