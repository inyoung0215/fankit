package com.fankit.settlement.integration

import com.fankit.settlement.infrastructure.persistence.PaymentRecordJpaEntity
import com.fankit.settlement.infrastructure.persistence.PaymentRecordJpaRepository
import com.fankit.settlement.infrastructure.persistence.SettlementDetailJpaRepository
import com.fankit.settlement.infrastructure.persistence.SettlementJpaRepository
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate
import java.time.ZoneId

/**
 * Settlement Batch Job 통합 테스트
 *
 * Scenario: 어제 결제 130건 (creator 2명) → settlementJob 실행
 *
 *   Step 1 (chunk=100): 130건이면 chunk 2회 (100건 + 30건)
 *     - SettlementDetail 130건 INSERT
 *     - PaymentRecord.settled = true UPDATE
 *
 *   Step 2 (Tasklet): creator별 집계
 *     - Settlement aggregate row 2건 INSERT (creator당 1건)
 *     - 각 row의 totalSales · totalCommission · netAmount 정확성
 *
 * 검증 포인트
 *   ① JobExecution.exitStatus == COMPLETED
 *   ② Step 1 readCount=130, writeCount=130, commitCount>=2 (chunk 동작 확인)
 *   ③ 모든 PaymentRecord settled=true
 *   ④ SettlementDetail 130건 (settlementId가 Step 2 후 채워짐)
 *   ⑤ Settlement 2건 — creator별 합산 정확
 */
@SpringBatchTest
class SettlementBatchJobIntegrationTest @Autowired constructor(
    private val jobLauncherTestUtils: JobLauncherTestUtils,
    private val paymentRecordRepository: PaymentRecordJpaRepository,
    private val settlementDetailRepository: SettlementDetailJpaRepository,
    private val settlementRepository: SettlementJpaRepository,
) : SettlementIntegrationTestSupport() {

    @BeforeEach
    fun seed() {
        // 깨끗한 상태 (이전 잔여 데이터 제거)
        settlementDetailRepository.deleteAll()
        settlementRepository.deleteAll()
        paymentRecordRepository.deleteAll()
    }

    @AfterEach
    fun cleanup() {
        settlementDetailRepository.deleteAll()
        settlementRepository.deleteAll()
        paymentRecordRepository.deleteAll()
    }

    @Test
    fun `결제 130건 → chunk 2회 처리 → Settlement aggregate creator별 1건씩 생성`() {
        // given — 어제 결제 130건 (creator 1: 80건, creator 2: 50건)
        val period = TARGET_DATE
        val periodStart = period.atStartOfDay(ZONE).toInstant()

        val records = buildList {
            // creator 1: 80건 × 1,000원 = 80,000원
            (1L..80L).forEach { i ->
                add(PaymentRecordJpaEntity(
                    paymentId = i, creatorId = 1L,
                    goodsId = "G-1", amount = 1_000,
                    approvedAt = periodStart.plusSeconds(i),
                ))
            }
            // creator 2: 50건 × 2,000원 = 100,000원
            (81L..130L).forEach { i ->
                add(PaymentRecordJpaEntity(
                    paymentId = i, creatorId = 2L,
                    goodsId = "G-2", amount = 2_000,
                    approvedAt = periodStart.plusSeconds(i),
                ))
            }
        }
        paymentRecordRepository.saveAll(records)

        // when — settlementJob 실행 (period = 어제)
        val jobParameters = JobParametersBuilder()
            .addString("period", period.toString())
            .addLong("runAt", System.currentTimeMillis())   // 동일 파라미터 재실행 방지
            .toJobParameters()
        val execution = jobLauncherTestUtils.launchJob(jobParameters)

        // then ① — Job 성공
        execution.status shouldBe BatchStatus.COMPLETED
        execution.exitStatus.exitCode shouldBe ExitStatus.COMPLETED.exitCode

        // then ② — Step 1: chunk=100이라 130건이면 2번 commit (100 + 30)
        val chunkStep = execution.stepExecutions.single { it.stepName == "settlementChunkStep" }
        chunkStep.readCount shouldBe 130
        chunkStep.writeCount shouldBe 130
        chunkStep.commitCount shouldBeGreaterThan 1L

        // Job listener가 processedRecordCount를 Job context로 승격
        execution.executionContext.getLong("processedRecordCount") shouldBe 130L

        // then ③ — 모든 PaymentRecord settled=true
        val unsettled = paymentRecordRepository.findAll().filter { !it.settled }
        unsettled shouldHaveSize 0

        // then ④ — SettlementDetail 130건, 모두 settlementId 채워짐
        val details = settlementDetailRepository.findAll()
        details shouldHaveSize 130
        details.count { it.settlementId == null } shouldBe 0

        // then ⑤ — Settlement aggregate 2건 (creator별)
        val settlements = settlementRepository.findAll().sortedBy { it.creatorId }
        settlements shouldHaveSize 2

        // creator 1: sales=80,000, commission=8,000 (10%), net=72,000
        val creator1 = settlements[0]
        creator1.creatorId shouldBe 1L
        creator1.period shouldBe period
        creator1.totalSales shouldBe 80_000L
        creator1.totalCommission shouldBe 8_000L
        creator1.netAmount shouldBe 72_000L

        // creator 2: sales=100,000, commission=10,000, net=90,000
        val creator2 = settlements[1]
        creator2.creatorId shouldBe 2L
        creator2.totalSales shouldBe 100_000L
        creator2.totalCommission shouldBe 10_000L
        creator2.netAmount shouldBe 90_000L
    }

    companion object {
        private val ZONE = ZoneId.of("Asia/Seoul")
        // 오늘에 의존하지 않는 고정 날짜 (테스트 안정성)
        private val TARGET_DATE: LocalDate = LocalDate.of(2026, 5, 18)
    }
}
