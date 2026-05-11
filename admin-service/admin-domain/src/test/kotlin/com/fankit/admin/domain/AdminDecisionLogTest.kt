package com.fankit.admin.domain

import com.fankit.admin.domain.model.AdminDecisionLog
import com.fankit.admin.domain.model.ApprovalDecision
import com.fankit.admin.domain.model.ApprovalType
import io.kotest.assertions.throwables.shouldThrow
import org.junit.jupiter.api.Test

class AdminDecisionLogTest {

    @Test
    fun `REJECTED 결정은 reason이 필수`() {
        shouldThrow<IllegalArgumentException> {
            AdminDecisionLog(
                type = ApprovalType.GOODS, targetId = "g1",
                decision = ApprovalDecision.REJECTED, decidedBy = 1L, reason = null,
            )
        }
    }

    @Test
    fun `APPROVED는 reason 없어도 OK`() {
        AdminDecisionLog(
            type = ApprovalType.GOODS, targetId = "g1",
            decision = ApprovalDecision.APPROVED, decidedBy = 1L,
        )
    }

    @Test
    fun `targetId는 비어있을 수 없다`() {
        shouldThrow<IllegalArgumentException> {
            AdminDecisionLog(
                type = ApprovalType.GOODS, targetId = "",
                decision = ApprovalDecision.APPROVED, decidedBy = 1L,
            )
        }
    }
}
