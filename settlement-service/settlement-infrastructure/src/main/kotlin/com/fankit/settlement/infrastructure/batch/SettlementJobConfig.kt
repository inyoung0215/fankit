package com.fankit.settlement.infrastructure.batch

import com.fankit.settlement.domain.model.PaymentRecord
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

// Settlement Job 구성:
//
//   Step 1 (chunk=100):
//     Reader (PaymentRecord) → Processor (commission 계산) → Writer (Detail INSERT + settled UPDATE)
//
//   Step 2 (Tasklet):
//     SQL group by로 Settlement aggregate row INSERT + Detail.settlement_id 채워넣기
//
// chunk=100 근거: 너무 작으면 commit 오버헤드 ↑, 너무 크면 실패 시 재시도 비용 ↑.
//                100~1000이 일반적 sweet spot. 운영 metric 보고 조정.
@Configuration
class SettlementJobConfig {

    @Bean
    fun settlementJob(
        jobRepository: JobRepository,
        listener: SettlementJobListener,
        chunkStep: Step,
        aggregateStep: Step,
    ): Job = JobBuilder("settlementJob", jobRepository)
        .listener(listener)
        .start(chunkStep)
        .next(aggregateStep)
        .build()

    @Bean
    fun chunkStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        reader: SettlementItemReader,
        processor: SettlementItemProcessor,
        writer: SettlementItemWriter,
    ): Step = StepBuilder("settlementChunkStep", jobRepository)
        .chunk<PaymentRecord, SettlementBatchItem>(100, transactionManager)
        .reader(reader)
        .processor(processor)
        .writer(writer)
        .build()

    @Bean
    fun aggregateStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        tasklet: AggregateSettlementsTasklet,
    ): Step = StepBuilder("settlementAggregateStep", jobRepository)
        .tasklet(tasklet, transactionManager)
        .build()
}
