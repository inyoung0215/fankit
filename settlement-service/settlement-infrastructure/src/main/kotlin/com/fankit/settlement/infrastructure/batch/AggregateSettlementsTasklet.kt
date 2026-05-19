package com.fankit.settlement.infrastructure.batch

import com.fankit.settlement.domain.model.Settlement
import com.fankit.settlement.domain.model.SettlementStatus
import com.fankit.settlement.infrastructure.persistence.SettlementDetailJpaRepository
import com.fankit.settlement.infrastructure.persistence.SettlementJpaEntity
import com.fankit.settlement.infrastructure.persistence.SettlementJpaRepository
import jakarta.persistence.EntityManager
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.LocalDate

// Step 2 — Step 1이 적재한 settlement_details에서 (creator_id) 단위로 집계
// → settlements row INSERT + settlement_details.settlement_id 채워넣기
//
// JPA bulk update query 사용. 단일 트랜잭션 (Spring Batch가 Tasklet에 트랜잭션 자동 적용)
//
// @StepScope 필수: @Value("#{jobParameters[...]}")로 jobParameters에 접근하려면
// Step 실행 컨텍스트가 필요. (SettlementItemReader도 같은 이유로 @StepScope)
@Component
@StepScope
class AggregateSettlementsTasklet(
    private val entityManager: EntityManager,
    private val settlementJpaRepository: SettlementJpaRepository,
    @Value("#{jobParameters['period']}") private val periodStr: String,
) : Tasklet {

    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus {
        val period = LocalDate.parse(periodStr)

        // creator별 sum (settlementId IS NULL 인 detail만)
        @Suppress("UNCHECKED_CAST")
        val rows = entityManager.createQuery(
            """
            SELECT d.creatorId AS creatorId,
                   SUM(d.salesAmount) AS totalSales,
                   SUM(d.commissionAmount) AS totalCommission
            FROM SettlementDetailJpaEntity d
            WHERE d.settlementId IS NULL
            GROUP BY d.creatorId
            """
        ).resultList as List<Array<Any>>

        var settlementCount = 0
        var totalNetAccum = 0L

        for (row in rows) {
            val creatorId = (row[0] as Number).toLong()
            val totalSales = (row[1] as Number).toLong()
            val totalCommission = (row[2] as Number).toLong()

            // (creatorId, period) unique — 이미 있으면 sum 더하기 (재실행 멱등 보강)
            val existing = settlementJpaRepository.findByCreatorIdAndPeriod(creatorId, period)
            val saved = if (existing != null) {
                existing.totalSales += totalSales
                existing.totalCommission += totalCommission
                existing.netAmount = existing.totalSales - existing.totalCommission
                settlementJpaRepository.save(existing)
            } else {
                val agg = Settlement(
                    creatorId = creatorId, period = period,
                    totalSales = totalSales, totalCommission = totalCommission,
                    netAmount = totalSales - totalCommission,
                    status = SettlementStatus.COMPLETED,
                )
                settlementJpaRepository.save(SettlementJpaEntity.fromDomain(agg))
            }

            // 해당 creator의 모든 NULL settlementId detail에 새로 만든 settlement.id를 채움
            entityManager.createQuery(
                """
                UPDATE SettlementDetailJpaEntity d
                SET d.settlementId = :sid
                WHERE d.creatorId = :cid AND d.settlementId IS NULL
                """
            )
                .setParameter("sid", saved.id!!)
                .setParameter("cid", creatorId)
                .executeUpdate()

            settlementCount++
            totalNetAccum += (totalSales - totalCommission)
        }

        // ExecutionContext에 통계 적재 → application 레이어가 결과 응답에 사용
        val ctx = chunkContext.stepContext.stepExecution.jobExecution.executionContext
        ctx.putInt("settlementCount", settlementCount)
        ctx.putLong("totalNetAmount", totalNetAccum)

        return RepeatStatus.FINISHED
    }
}
