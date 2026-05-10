package com.fankit.settlement.infrastructure.quartz

import com.fankit.settlement.domain.port.inbound.RunSettlementUseCase
import org.quartz.JobExecutionContext
import org.slf4j.LoggerFactory
import org.springframework.scheduling.quartz.QuartzJobBean
import java.time.LocalDate
import java.time.ZoneId

// Quartz가 trigger 도달 시점에 호출하는 Job
// 도메인 use case (RunSettlementUseCase)를 호출 — 인프라 트리거가 도메인을 호출하는 표준 패턴
class SettlementQuartzJob : QuartzJobBean() {

    private val log = LoggerFactory.getLogger(javaClass)

    // QuartzJobBean이 SchedulerContext에서 주입 — QuartzConfig에서 설정
    lateinit var runSettlementUseCase: RunSettlementUseCase

    override fun executeInternal(context: JobExecutionContext) {
        // 정산 대상 = 전일 (KST)
        val period = LocalDate.now(ZoneId.of("Asia/Seoul")).minusDays(1)
        log.info("[Quartz] Settlement Job 시작 period={}", period)
        val result = runSettlementUseCase.run(period)
        log.info(
            "[Quartz] Settlement Job 완료 processed={} settlements={} totalNet={}",
            result.processedRecordCount, result.settlementCount, result.totalNetAmount,
        )
    }
}
