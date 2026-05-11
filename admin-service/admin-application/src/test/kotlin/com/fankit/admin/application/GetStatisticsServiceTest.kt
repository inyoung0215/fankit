package com.fankit.admin.application

import com.fankit.admin.domain.model.ApprovalDecision
import com.fankit.admin.domain.model.ApprovalType
import com.fankit.admin.domain.port.outbound.AdminDecisionLogRepository
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

class GetStatisticsServiceTest {

    private val logRepository: AdminDecisionLogRepository = mockk()
    private val service = GetStatisticsService(logRepository)

    @Test
    fun `summary는 type별 approved, rejected 카운트와 총합 집계`() {
        every { logRepository.countBy(ApprovalType.GOODS, ApprovalDecision.APPROVED) } returns 10L
        every { logRepository.countBy(ApprovalType.GOODS, ApprovalDecision.REJECTED) } returns 3L
        every { logRepository.countBy(ApprovalType.CREATOR_TRANSITION, ApprovalDecision.APPROVED) } returns 5L
        every { logRepository.countBy(ApprovalType.CREATOR_TRANSITION, ApprovalDecision.REJECTED) } returns 1L

        val s = service.summary()

        s.totalApproved shouldBe 15L
        s.totalRejected shouldBe 4L
        s.byType[ApprovalType.GOODS]!!.approved shouldBe 10L
        s.byType[ApprovalType.GOODS]!!.rejected shouldBe 3L
        s.byType[ApprovalType.CREATOR_TRANSITION]!!.total shouldBe 6L
    }
}
