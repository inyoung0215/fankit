package com.fankit.payment.application.service

import com.fankit.payment.application.saga.PaymentSagaOrchestrator
import com.fankit.payment.domain.model.PaymentResult
import com.fankit.payment.domain.port.inbound.CreatePaymentCommand
import com.fankit.payment.domain.port.inbound.CreatePaymentUseCase
import org.springframework.stereotype.Service

// 3-Phase 결제 처리 — 트랜잭션 경계를 명확히 분리
//
//   Phase A (DB tx): Idempotency 이중 체크 + 비관적 락 + Payment row 생성
//   Phase B (tx 밖): Saga 실행 (PG/Order/Goods 외부 호출)
//   Phase C (DB tx): 결과 반영 + Outbox 적재
//
// 분리 이유: Saga 외부 호출은 수 초~수십 초 걸릴 수 있음.
//          한 트랜잭션에 묶으면 DB 커넥션 점유 → 풀 고갈 + lock 장기 보유 → 다른 결제 차단
@Service
class CreatePaymentService(
    private val preparation: PaymentPreparationService,
    private val sagaOrchestrator: PaymentSagaOrchestrator,
    private val finalization: PaymentFinalizationService,
) : CreatePaymentUseCase {

    override fun create(command: CreatePaymentCommand): PaymentResult {
        // Phase A
        val prepared = preparation.prepare(command)
        if (prepared.alreadyProcessed) {
            // Idempotency Key 재사용 — 이전 결과 그대로 반환 (멱등성 보장)
            val p = prepared.payment
            return PaymentResult.Success(p.id!!, p.amount, p.pgTransactionId ?: "")
        }

        // Phase B
        val sagaResult = sagaOrchestrator.execute(prepared.payment, command.items)

        // Phase C
        return finalization.finalize(prepared.payment, sagaResult)
    }
}
