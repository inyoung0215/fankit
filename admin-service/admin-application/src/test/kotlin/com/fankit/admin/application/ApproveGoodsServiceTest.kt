package com.fankit.admin.application

import com.fankit.admin.domain.model.AdminDecisionLog
import com.fankit.admin.domain.model.ApprovalDecision
import com.fankit.admin.domain.model.ApprovalType
import com.fankit.admin.domain.port.outbound.AdminDecisionLogRepository
import com.fankit.admin.domain.port.outbound.GoodsAdminClient
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class ApproveGoodsServiceTest {

    private val client: GoodsAdminClient = mockk()
    private val logRepository: AdminDecisionLogRepository = mockk()
    private val service = ApproveGoodsService(client, logRepository)

    @Test
    fun `정상 승인 — 외부 호출 후 로그 적재`() {
        every { client.approveGoods("g1") } just Runs
        every { logRepository.save(any()) } answers { firstArg<AdminDecisionLog>().copy(id = 100L) }

        val result = service.approve("g1", adminId = 7L)

        result.id shouldBe 100L
        result.type shouldBe ApprovalType.GOODS
        result.decision shouldBe ApprovalDecision.APPROVED
        verify { client.approveGoods("g1") }
        verify { logRepository.save(match { it.targetId == "g1" && it.decidedBy == 7L }) }
    }

    @Test
    fun `외부 호출 실패 시 로그 적재 안 됨`() {
        every { client.approveGoods("g1") } throws RuntimeException("Goods Service down")

        shouldThrow<RuntimeException> { service.approve("g1", 7L) }

        verify(exactly = 0) { logRepository.save(any()) }
    }
}
