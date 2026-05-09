package com.fankit.payment.application.saga

import com.fankit.payment.domain.model.Payment
import com.fankit.payment.domain.model.SagaLog
import com.fankit.payment.domain.model.SagaStatus
import com.fankit.payment.domain.model.SagaStep
import com.fankit.payment.domain.port.inbound.StockLineCommand
import com.fankit.payment.domain.port.outbound.GoodsServiceClient
import com.fankit.payment.domain.port.outbound.OrderServiceClient
import com.fankit.payment.domain.port.outbound.PgApprovalRequest
import com.fankit.payment.domain.port.outbound.PgClient
import com.fankit.payment.domain.port.outbound.SagaLogRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.UUID

// Orchestration Saga
//
//   step 1: PG 승인        (보상 = pgClient.cancel)
//   step 2: 주문 확정       (보상 = orderClient.cancel)
//   step 3: 재고 차감       (보상 = goodsClient.restoreStock)
//
// 보상은 LIFO 순서 — step N이 실패하면 step N-1, N-2... 역방향으로 보상 실행
// 외부 호출만 하고 DB 트랜잭션은 잡지 않는다 (커넥션 점유 X)
@Component
class PaymentSagaOrchestrator(
    private val pgClient: PgClient,
    private val orderServiceClient: OrderServiceClient,
    private val goodsServiceClient: GoodsServiceClient,
    private val sagaLogRepository: SagaLogRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun execute(payment: Payment, items: List<StockLineCommand>): SagaResult {
        val sagaId = UUID.randomUUID().toString()
        var sagaLog = sagaLogRepository.save(
            SagaLog(
                sagaId = sagaId, paymentId = payment.id,
                status = SagaStatus.STARTED, currentStep = SagaStep.PAYMENT_APPROVE,
            )
        )

        // ─── step 1: PG 승인 ────────────────────────────────────
        val pgResponse = try {
            pgClient.approve(PgApprovalRequest(payment.orderId, payment.amount, payment.method))
        } catch (e: Exception) {
            log.warn("[Saga {}] PG 승인 실패: {}", sagaId, e.message)
            sagaLogRepository.save(sagaLog.copy(status = SagaStatus.COMPENSATED, failureReason = "PG: ${e.message}"))
            // PG 자체가 실패 — 보상할 게 없음 (이전 step 없음)
            return SagaResult.Failure("PG 승인 실패: ${e.message}")
        }
        sagaLog = sagaLogRepository.save(sagaLog.copy(currentStep = SagaStep.ORDER_CONFIRM))

        // ─── step 2: 주문 확정 ───────────────────────────────────
        try {
            orderServiceClient.confirm(payment.orderId)
        } catch (e: Exception) {
            log.warn("[Saga {}] 주문 확정 실패, PG 취소 보상 진행: {}", sagaId, e.message)
            return compensate(
                sagaLog = sagaLog,
                actions = listOf(SafeAction("PG 취소") { pgClient.cancel(pgResponse.pgTransactionId) }),
                reason = "주문 확정 실패: ${e.message}",
                pgTransactionId = pgResponse.pgTransactionId,
            )
        }
        sagaLog = sagaLogRepository.save(sagaLog.copy(currentStep = SagaStep.STOCK_DECREASE))

        // ─── step 3: 재고 차감 ───────────────────────────────────
        val stockLines = items.map { GoodsServiceClient.StockLine(it.goodsId, it.quantity) }
        try {
            goodsServiceClient.decreaseStock(stockLines)
        } catch (e: Exception) {
            log.warn("[Saga {}] 재고 차감 실패, 주문/PG 보상 진행: {}", sagaId, e.message)
            return compensate(
                sagaLog = sagaLog,
                // LIFO: 주문 취소 → PG 취소 순서
                actions = listOf(
                    SafeAction("주문 취소") { orderServiceClient.cancel(payment.orderId) },
                    SafeAction("PG 취소") { pgClient.cancel(pgResponse.pgTransactionId) },
                ),
                reason = "재고 차감 실패: ${e.message}",
                pgTransactionId = pgResponse.pgTransactionId,
            )
        }

        // 모든 step 성공
        sagaLogRepository.save(sagaLog.copy(status = SagaStatus.COMPLETED, currentStep = SagaStep.DONE))
        return SagaResult.Success(pgResponse.pgTransactionId)
    }

    private fun compensate(
        sagaLog: SagaLog,
        actions: List<SafeAction>,
        reason: String,
        pgTransactionId: String,
    ): SagaResult {
        sagaLogRepository.save(sagaLog.copy(status = SagaStatus.COMPENSATING, failureReason = reason))

        // 모든 보상 액션을 시도. 일부 실패해도 끝까지 진행 (가능한 만큼 되돌림)
        // 보상이 모두 실패하면 COMPENSATION_FAILED — 운영 알람 + 수동 개입 필요
        val failedActions = mutableListOf<String>()
        for (action in actions) {
            try {
                action.run()
            } catch (e: Exception) {
                log.error("[Saga {}] 보상 실패 [{}]: {}", sagaLog.sagaId, action.name, e.message)
                failedActions += action.name
            }
        }

        val finalStatus = if (failedActions.isEmpty()) SagaStatus.COMPENSATED
        else SagaStatus.COMPENSATION_FAILED
        sagaLogRepository.save(sagaLog.copy(
            status = finalStatus,
            failureReason = if (failedActions.isEmpty()) reason
            else "$reason | 보상 실패: ${failedActions.joinToString(",")}",
        ))

        return SagaResult.Failure(reason, pgTransactionId)
    }

    private class SafeAction(val name: String, val block: () -> Unit) {
        fun run() = block()
    }
}
