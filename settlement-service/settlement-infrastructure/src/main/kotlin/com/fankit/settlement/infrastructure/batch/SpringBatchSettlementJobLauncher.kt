package com.fankit.settlement.infrastructure.batch

import com.fankit.settlement.domain.port.outbound.SettlementBatchJobLauncher
import com.fankit.settlement.domain.port.outbound.SettlementBatchResult
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.stereotype.Component
import java.time.LocalDate

// Spring Batch 어댑터 — JobLauncher.run()을 SettlementBatchJobLauncher port로 노출
//
// 매 실행마다 startedAt timestamp를 unique 파라미터로 추가
//   → 같은 period 재실행 가능 (PaymentRecord.settled=true 기준으로 멱등성은 자체 보장)
@Component
class SpringBatchSettlementJobLauncher(
    private val jobLauncher: JobLauncher,
    private val settlementJob: Job,
) : SettlementBatchJobLauncher {

    override fun runSettlementBatch(period: LocalDate): SettlementBatchResult {
        val params = JobParametersBuilder()
            .addString("period", period.toString())
            .addLong("startedAt", System.currentTimeMillis())
            .toJobParameters()

        val execution = jobLauncher.run(settlementJob, params)
        val ctx = execution.executionContext

        return SettlementBatchResult(
            processedRecordCount = ctx.getLong("processedRecordCount", 0L),
            settlementCount = ctx.getInt("settlementCount", 0),
            totalNetAmount = ctx.getLong("totalNetAmount", 0L),
        )
    }
}
