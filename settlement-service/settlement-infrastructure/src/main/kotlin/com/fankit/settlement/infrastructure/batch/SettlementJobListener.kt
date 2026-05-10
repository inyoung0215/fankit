package com.fankit.settlement.infrastructure.batch

import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobExecutionListener
import org.springframework.stereotype.Component

// Step 1의 처리량을 Job ExecutionContext로 승격 (Step 간 통계 합산)
@Component
class SettlementJobListener : JobExecutionListener {

    override fun afterJob(jobExecution: JobExecution) {
        // Step 1의 readCount를 Job context로 승격
        val processed = jobExecution.stepExecutions
            .filter { it.stepName == "settlementChunkStep" }
            .sumOf { it.readCount }
        jobExecution.executionContext.putLong("processedRecordCount", processed)
    }
}
