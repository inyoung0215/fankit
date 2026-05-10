package com.fankit.settlement.application

import com.fankit.settlement.domain.port.inbound.RunSettlementResult
import com.fankit.settlement.domain.port.inbound.RunSettlementUseCase
import com.fankit.settlement.domain.port.outbound.SettlementBatchJobLauncher
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate

// 정산 배치 트리거 use case 구현
//
// 핵심: Spring Batch의 JobLauncher를 직접 의존하지 않고 SettlementBatchJobLauncher port만 의존
//   → application은 인프라 기술을 모르고, batch framework 교체에도 이 코드 변경 0
@Service
class RunSettlementService(
    private val launcher: SettlementBatchJobLauncher,
) : RunSettlementUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun run(period: LocalDate): RunSettlementResult {
        log.info("[Settlement] 트리거 period={}", period)
        val r = launcher.runSettlementBatch(period)
        return RunSettlementResult(
            period = period,
            processedRecordCount = r.processedRecordCount,
            settlementCount = r.settlementCount,
            totalNetAmount = r.totalNetAmount,
        )
    }
}
