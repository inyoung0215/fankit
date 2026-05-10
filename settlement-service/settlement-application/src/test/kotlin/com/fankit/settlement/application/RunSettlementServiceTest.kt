package com.fankit.settlement.application

import com.fankit.settlement.domain.port.outbound.SettlementBatchJobLauncher
import com.fankit.settlement.domain.port.outbound.SettlementBatchResult
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.LocalDate

class RunSettlementServiceTest {

    private val launcher: SettlementBatchJobLauncher = mockk()
    private val service = RunSettlementService(launcher)

    @Test
    fun `run은 SettlementBatchJobLauncher를 호출하고 결과를 변환해 반환`() {
        val period = LocalDate.of(2026, 5, 9)
        every { launcher.runSettlementBatch(period) } returns
            SettlementBatchResult(processedRecordCount = 1234L, settlementCount = 7, totalNetAmount = 9_876_543L)

        val result = service.run(period)

        result.period shouldBe period
        result.processedRecordCount shouldBe 1234L
        result.settlementCount shouldBe 7
        result.totalNetAmount shouldBe 9_876_543L
        verify { launcher.runSettlementBatch(period) }
    }
}
